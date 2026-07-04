package com.example.antiagingreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.antiagingreminder.AntiAgingApp
import com.example.antiagingreminder.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 闹钟触发广播接收器。
 * 系统在到点时唤醒并调用 onReceive，此处弹出通知，并重新排程下一次提醒。
 *
 * 优化：使用 goAsync() 延长 BroadcastReceiver 生命周期，
 * 在 IO 线程中异步完成通知显示与下次排程，避免 ANR。
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_FIRE = "com.example.antiagingreminder.ACTION_FIRE"
        const val EXTRA_PLAN_ID = "extra_plan_id"
        const val EXTRA_TIME_ID = "extra_time_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val planId = intent.getLongExtra(EXTRA_PLAN_ID, -1L)
        val timeId = intent.getLongExtra(EXTRA_TIME_ID, 0L)
        val fallbackTitle = intent.getStringExtra(EXTRA_TITLE) ?: "抗衰养生提醒"
        val fallbackContent = intent.getStringExtra(EXTRA_CONTENT) ?: "该进行今日健康计划啦"

        // 异步处理：校验计划 + 显示通知 + 重新排程
        // goAsync() 给予约 10 秒窗口，添加 8 秒超时确保在窗口内完成
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(8_000) {
                    val plan = AppDatabase.get(context).planDao().getPlanById(planId)
                    if (plan != null && plan.isActive) {
                        // 使用数据库最新值而非 Intent 中的快照，避免用户修改后显示旧标题
                        val title = plan.title
                        val content = plan.description.ifBlank { fallbackContent }
                        // 复用 AppContainer 中的单例，避免重复创建
                        val container = (context.applicationContext as? AntiAgingApp)?.container
                        val helper = container?.notificationHelper ?: NotificationHelper(context)
                        helper.createChannel()
                        helper.showNotification(planId, timeId, title, content)
                        (container?.alarmScheduler ?: AlarmSchedulerImpl(context)).rescheduleAll()
                    }
                }
            } catch (e: Exception) {
                // 静默处理异常，确保 pending.finish() 被调用
            } finally {
                try { pending.finish() } catch (_: Exception) {}
            }
        }
    }
}
