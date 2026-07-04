package com.example.antiagingreminder.domain

import com.example.antiagingreminder.data.PlanType

/**
 * 时间轴条目模型（UI 层使用）。
 * 由计划、时间点与历史完成状态计算得出，按时间从早到晚排列。
 */
data class TimelineItem(
    val planId: Long,
    val reminderTimeId: Long,
    val title: String,
    val description: String,
    val type: PlanType,
    val hour: Int,
    val minute: Int,
    val isCompleted: Boolean
) {
    /** 格式化时间，如 07:05 */
    val timeText: String
        get() = "%02d:%02d".format(hour, minute)

    /** 当天总分钟数，便于排序与比较 */
    val minutesOfDay: Int
        get() = hour * 60 + minute
}
