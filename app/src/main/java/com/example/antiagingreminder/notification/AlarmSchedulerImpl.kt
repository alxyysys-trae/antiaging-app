package com.example.antiagingreminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.antiagingreminder.data.local.AppDatabase
import com.example.antiagingreminder.data.local.entity.PlanEntity
import com.example.antiagingreminder.data.local.entity.ReminderTimeEntity
import com.example.antiagingreminder.util.DateTimeUtils
import java.util.Calendar

/**
 * 闹钟调度实现：基于 AlarmManager 的精确闹钟保证时间准确。
 * 应用在后台或被杀死后，已注册的精确闹钟仍会被系统触发。
 *
 * 排程策略：为每条「活动计划 + 时间点」计算下一次触发时刻并注册一次闹钟；
 * 闹钟触发后由 [AlarmReceiver] 弹出通知并重新排程下一次，循环推进。
 *
 * ColorOS 适配：
 * - 无精确闹钟权限时回退为非精确闹钟，保证提醒仍可弹出
 * - cancelPlan 改为 suspend，避免 runBlocking 阻塞主线程
 *
 * 性能优化：rescheduleAll 使用单次 JOIN 查询获取全部时间点，
 * 避免对每个计划单独查询（N+1 问题）。
 */
class AlarmSchedulerImpl(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** 请求码生成：由 planId 与 timeId 组合得到稳定唯一整数（避免哈希碰撞） */
    private fun requestCode(planId: Long, timeId: Long): Int {
        // 高 16 位放 planId，低 16 位放 timeId，确保唯一性
        return ((planId.toInt() and 0xFFFF) shl 16) or (timeId.toInt() and 0xFFFF)
    }

    /**
     * 取消某计划相关的全部闹钟。
     * 查询该计划的全部时间点并逐一取消 PendingIntent。
     * 已改为 suspend，避免 runBlocking 阻塞主线程。
     */
    override suspend fun cancelPlan(planId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
        }
        val times = AppDatabase.get(context).planDao().getTimesForPlan(planId)
        times.forEach { time ->
            val pi = PendingIntent.getBroadcast(
                context,
                requestCode(planId, time.id),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            if (pi != null) alarmManager.cancel(pi)
        }
    }

    /**
     * 重新排程全部提醒。
     * 从数据库读取活动计划与时间点，为每条计算下一次触发时间并注册精确闹钟。
     *
     * 性能优化：使用 getAllActiveTimes 一次性查询全部活动时间点，
     * 避免对每个计划单独查询。
     */
    override suspend fun rescheduleAll() {
        val db = AppDatabase.get(context)
        val plans = db.planDao().getActivePlans()
        // 一次性查询全部活动计划的时间点，按 planId 分组
        val allTimes = db.planDao().getAllActiveTimes()
        val timesByPlan: Map<Long, List<ReminderTimeEntity>> = allTimes.groupBy { it.planId }

        // 先取消全部已注册闹钟
        plans.forEach { plan ->
            timesByPlan[plan.id]?.forEach { time ->
                cancelAlarm(plan.id, time.id)
            }
        }

        // 重新注册
        val now = Calendar.getInstance()
        plans.forEach { plan ->
            val days = DateTimeUtils.parseDays(plan.repeatDays)
            timesByPlan[plan.id]?.forEach { time ->
                scheduleNext(plan, time, days, now)
            }
        }
    }

    /** 计算并注册某条提醒的下一次触发 */
    private fun scheduleNext(
        plan: PlanEntity,
        time: ReminderTimeEntity,
        repeatDays: Set<Int>,
        now: Calendar
    ) {
        val triggerAt = computeNextFireTime(
            repeatDays = repeatDays,
            hour = time.hour,
            minute = time.minute,
            now = now
        ) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
            putExtra(AlarmReceiver.EXTRA_PLAN_ID, plan.id)
            putExtra(AlarmReceiver.EXTRA_TIME_ID, time.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, plan.title)
            putExtra(AlarmReceiver.EXTRA_CONTENT, plan.description)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode(plan.id, time.id),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // API 31+ 需检查精确闹钟权限；无权限时回退为非精确闹钟，保证提醒仍可弹出
        // ColorOS 适配：setExactAndAllowWhileIdle 已包含 AllowWhileIdle 语义，
        // 可在 Doze 模式下唤醒；无需额外注册兜底闹钟，
        // WorkManager AlarmFallbackWorker 每 30 分钟补注册一次作为兜底
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // 主：精确闹钟
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAt, pi
                )
            } else {
                // 回退：非精确闹钟（延迟最多几分钟）
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pi
            )
        }
    }

    /** 取消某条闹钟 */
    private fun cancelAlarm(planId: Long, timeId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode(planId, timeId),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pi != null) alarmManager.cancel(pi)
    }

    companion object {
        /**
         * 计算下一次触发时间（毫秒）。
         * 在今天起 8 天内寻找满足「星期在 repeatDays 中 + 时刻为 hour:minute 且晚于 now」的最早时刻。
         * repeatDays 为空表示每天都触发。
         */
        fun computeNextFireTime(
            repeatDays: Set<Int>,
            hour: Int,
            minute: Int,
            now: Calendar
        ): Long? {
            val days = if (repeatDays.isEmpty()) (1..7).toSet() else repeatDays
            for (offset in 0..7) {
                val candidate = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                // 转为 ISO 星期（1=周一…7=周日）
                val dow = candidate.get(Calendar.DAY_OF_WEEK)
                val iso = if (dow == Calendar.SUNDAY) 7 else dow - 1
                if (iso in days && candidate.timeInMillis > now.timeInMillis) {
                    return candidate.timeInMillis
                }
            }
            return null
        }
    }
}
