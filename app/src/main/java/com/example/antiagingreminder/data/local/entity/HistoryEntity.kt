package com.example.antiagingreminder.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 历史完成记录实体。
 * 以「计划 + 时间点 + 日期」为唯一约束，记录某条提醒在某天是否完成。
 * 用于时间轴勾选与历史页面统计。
 *
 * 性能优化：添加 date 单列索引，加速 observeByDate 高频查询。
 *
 * @param date 日期字符串，格式 yyyy-MM-dd
 */
@Entity(
    tableName = "history",
    indices = [
        Index(value = ["planId", "reminderTimeId", "date"], unique = true),
        Index(value = ["date"])  // 加速按日期查询
    ]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val reminderTimeId: Long,
    val date: String,
    val isCompleted: Boolean,
    val completedAt: Long? = null
)
