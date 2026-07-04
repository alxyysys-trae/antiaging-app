package com.example.antiagingreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 提醒时间点实体。
 * 一个计划可拥有多个精确到分钟的时间点（如 07:00、12:30）。
 * 删除计划时联动删除其所有时间点。
 */
@Entity(
    tableName = "reminder_times",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class ReminderTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val hour: Int,     // 0..23
    val minute: Int    // 0..59
)
