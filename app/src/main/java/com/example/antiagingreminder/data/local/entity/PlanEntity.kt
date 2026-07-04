package com.example.antiagingreminder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 提醒计划实体。
 * 一个计划包含标题、描述、类型，以及可被重复利用的元信息。
 * 具体的提醒时间点存储在 [ReminderTimeEntity] 中（一个计划可拥有多个时间点）。
 *
 * 性能优化：添加 isActive 索引，加速活动计划查询。
 *
 * @param repeatDays 重复星期，使用 ISO 日历（1=周一 … 7=周日），
 *                   以逗号分隔字符串保存，如 "1,3,5"；为空表示每天都重复。
 */
@Entity(
    tableName = "plans",
    indices = [Index(value = ["isActive"])]  // 加速活动计划筛选
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val type: String,
    val isActive: Boolean = true,
    val repeatDays: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
