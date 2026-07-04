package com.example.antiagingreminder.data.repository

import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.data.local.dao.HistoryDao
import com.example.antiagingreminder.data.local.dao.PlanDao
import com.example.antiagingreminder.data.local.dao.TemplateDao
import com.example.antiagingreminder.data.local.entity.HistoryEntity
import com.example.antiagingreminder.data.local.entity.PlanEntity
import com.example.antiagingreminder.data.local.entity.ReminderTimeEntity
import com.example.antiagingreminder.data.local.entity.TemplateEntity
import com.example.antiagingreminder.domain.PlanDetail
import com.example.antiagingreminder.domain.PlanTime
import com.example.antiagingreminder.domain.TimelineItem
import com.example.antiagingreminder.notification.AlarmScheduler
import com.example.antiagingreminder.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 数据仓库层：统一封装数据访问，向上提供领域模型与 Flow。
 * 协调 PlanDao / HistoryDao / TemplateDao，并在数据变更后触发通知重排。
 *
 * 依赖 [AlarmScheduler] 接口而非具体实现，符合依赖倒置原则。
 *
 * 性能优化：
 * - 使用批量查询避免 N+1 问题
 * - Flow 链添加 distinctUntilChanged 减少不必要的 UI 重组
 */
class PlanRepository(
    private val planDao: PlanDao,
    private val historyDao: HistoryDao,
    private val templateDao: TemplateDao,
    private val alarmScheduler: AlarmScheduler
) {

    // ---------------- 模板 ----------------

    /** 观察全部模板 */
    fun observeTemplates(): Flow<List<TemplateEntity>> =
        templateDao.observeAll().distinctUntilChanged()

    // ---------------- 计划列表 ----------------

    /**
     * 观察全部计划（带时间点），按创建时间倒序。
     * 性能优化：一次性查询全部时间点并按 planId 分组，避免 N+1。
     */
    fun observePlansWithTimes(): Flow<List<PlanDetail>> =
        planDao.observeAllPlans().map { plans ->
            // 批量查询所有时间点（非每个计划单独查）
            val allTimes = planDao.getAllTimes()
            val timesByPlan = allTimes.groupBy { it.planId }
            plans.map { plan ->
                plan.toDetail(timesByPlan[plan.id] ?: emptyList())
            }
        }.distinctUntilChanged()

    /** 获取单个计划的详情（含时间点） */
    suspend fun getPlanDetail(planId: Long): PlanDetail? {
        val plan = planDao.getPlanById(planId) ?: return null
        val times = planDao.getTimesForPlan(planId)
        return PlanDetail(
            id = plan.id,
            title = plan.title,
            description = plan.description,
            type = PlanType.fromName(plan.type),
            isActive = plan.isActive,
            repeatDays = DateTimeUtils.parseDays(plan.repeatDays),
            times = times.map { PlanTime(it.id, it.hour, it.minute) }
        )
    }

    /**
     * 新建计划。返回新计划 id。
     * 创建后会为该计划的所有时间点注册精确闹钟。
     */
    suspend fun createPlan(detail: PlanDetail): Long {
        val planId = planDao.insertPlan(
            PlanEntity(
                title = detail.title,
                description = detail.description,
                type = detail.type.name,
                isActive = detail.isActive,
                repeatDays = DateTimeUtils.daysToString(detail.repeatDays)
            )
        )
        planDao.replaceTimes(planId, detail.times.map { ReminderTimeEntity(0, planId, it.hour, it.minute) })
        alarmScheduler.rescheduleAll()
        return planId
    }

    /**
     * 更新计划（含时间点替换），并重新排程提醒。
     *
     * Bug 1 修复：保留原始 createdAt，避免编辑后排序错乱。
     * Bug 2 修复：智能匹配旧时间点保留 ID，避免历史完成记录孤立。
     * Bug 3 修复：先 cancelPlan 取消旧闹钟，避免残留闹钟在旧时间触发。
     */
    suspend fun updatePlan(detail: PlanDetail) {
        // 先取消旧闹钟（此时旧 timeId 还在 DB 中，cancelPlan 能查到并取消）
        alarmScheduler.cancelPlan(detail.id)

        // 查询原始 plan 保留 createdAt
        val existing = planDao.getPlanById(detail.id)
        planDao.updatePlan(
            PlanEntity(
                id = detail.id,
                title = detail.title,
                description = detail.description,
                type = detail.type.name,
                isActive = detail.isActive,
                repeatDays = DateTimeUtils.daysToString(detail.repeatDays),
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
        )

        // 智能匹配旧时间点：相同 hour:minute 保留旧 ID，避免历史记录孤立
        // 使用可变列表移除已匹配项，防止重复时间点匹配到同一旧 ID
        val remaining = planDao.getTimesForPlan(detail.id).toMutableList()
        val updatedTimes = detail.times.map { newTime ->
            val idx = remaining.indexOfFirst { it.hour == newTime.hour && it.minute == newTime.minute }
            val matched = if (idx >= 0) remaining.removeAt(idx) else null
            ReminderTimeEntity(
                id = matched?.id ?: 0,
                planId = detail.id,
                hour = newTime.hour,
                minute = newTime.minute
            )
        }
        planDao.replaceTimes(detail.id, updatedTimes)
        alarmScheduler.rescheduleAll()
    }

    /** 删除计划，取消其相关提醒 */
    suspend fun deletePlan(planId: Long) {
        alarmScheduler.cancelPlan(planId)
        planDao.deletePlanById(planId)
        alarmScheduler.rescheduleAll()
    }

    /** 从模板一键添加为计划，添加后可自由修改 */
    suspend fun addPlanFromTemplate(template: TemplateEntity): Long {
        val planId = planDao.insertPlan(
            PlanEntity(
                title = template.title,
                description = template.description,
                type = template.type,
                isActive = true,
                repeatDays = template.repeatDays
            )
        )
        val times = DateTimeUtils.parseTimes(template.times)
            .map { ReminderTimeEntity(0, planId, it.first, it.second) }
        planDao.insertTimes(times)
        alarmScheduler.rescheduleAll()
        return planId
    }

    // ---------------- 今日时间轴 ----------------

    /**
     * 观察指定日期的全部提醒条目（按时间升序）。
     * 结合活动计划、时间点与历史完成状态计算得出。
     *
     * 修复：接受 date 参数，支持跨天刷新。
     * 性能优化：一次性查询全部时间点并按 planId 分组，避免 N+1。
     */
    fun observeTodayTimeline(date: String = DateTimeUtils.today()): Flow<List<TimelineItem>> = combine(
        planDao.observeAllPlans(),
        historyDao.observeByDate(date)
    ) { plans, histories ->
        val iso = DateTimeUtils.isoDayOfWeekForDate(date)
        val completedKeys = histories.filter { it.isCompleted }
            .map { it.reminderTimeId }
            .toSet()

        // 批量查询活动计划的时间点，避免对每个计划单独查询（N+1 → 1 次）
        val allTimes = planDao.getAllActiveTimes()
        val timesByPlan = allTimes.groupBy { it.planId }

        plans.filter { it.isActive }
            .filter { plan ->
                // 未设置重复日表示每天执行；否则需包含该日期对应的星期
                val days = DateTimeUtils.parseDays(plan.repeatDays)
                days.isEmpty() || iso in days
            }
            .flatMap { plan ->
                (timesByPlan[plan.id] ?: emptyList()).map { time ->
                    TimelineItem(
                        planId = plan.id,
                        reminderTimeId = time.id,
                        title = plan.title,
                        description = plan.description,
                        type = PlanType.fromName(plan.type),
                        hour = time.hour,
                        minute = time.minute,
                        isCompleted = time.id in completedKeys
                    )
                }
            }
            .sortedBy { it.minutesOfDay }
    }.distinctUntilChanged()

    // ---------------- 历史记录 ----------------

    /** 观察有记录的全部日期 */
    fun observeHistoryDates(): Flow<List<String>> =
        historyDao.observeDates().distinctUntilChanged()

    /** 观察某日期的完成记录 */
    fun observeHistoryByDate(date: String): Flow<List<HistoryEntity>> =
        historyDao.observeByDate(date).distinctUntilChanged()

    /** 切换某条提醒在指定日期的完成状态（事务内原子操作，避免竞态） */
    suspend fun toggleComplete(planId: Long, reminderTimeId: Long, date: String) {
        historyDao.toggleComplete(planId, reminderTimeId, date, System.currentTimeMillis())
    }

    // ---------------- 内部工具 ----------------

    private fun PlanEntity.toDetail(times: List<ReminderTimeEntity>): PlanDetail =
        PlanDetail(
            id = id,
            title = title,
            description = description,
            type = PlanType.fromName(type),
            isActive = isActive,
            repeatDays = DateTimeUtils.parseDays(repeatDays),
            times = times.map { PlanTime(it.id, it.hour, it.minute) }
        )
}
