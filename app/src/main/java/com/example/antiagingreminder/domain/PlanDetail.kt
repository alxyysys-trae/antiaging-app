package com.example.antiagingreminder.domain

import com.example.antiagingreminder.data.PlanType

/**
 * 计划与其全部时间点的聚合模型，用于编辑/列表页面展示与修改。
 */
data class PlanDetail(
    val id: Long,
    val title: String,
    val description: String,
    val type: PlanType,
    val isActive: Boolean,
    val repeatDays: Set<Int>,   // 1=周一 … 7=周日，空集合表示每天
    val times: List<PlanTime>   // 时间点列表
)

data class PlanTime(
    val id: Long,
    val hour: Int,
    val minute: Int
) {
    val text: String get() = "%02d:%02d".format(hour, minute)
}
