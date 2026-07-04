package com.example.antiagingreminder.notification

/**
 * 闹钟调度抽象接口。
 * 数据层（仓库）通过该接口在计划增删改后重新排程通知，
 * 从而避免数据层直接依赖 Android 闹钟 API，保持分层清晰。
 *
 * 注意：cancelPlan 改为 suspend，避免在主线程阻塞（原实现使用 runBlocking）。
 */
interface AlarmScheduler {
    /** 根据当前所有计划重新排程全部提醒 */
    suspend fun rescheduleAll()

    /** 取消某计划相关的全部提醒 */
    suspend fun cancelPlan(planId: Long)
}
