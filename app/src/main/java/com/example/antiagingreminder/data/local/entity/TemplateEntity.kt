package com.example.antiagingreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 模板实体。
 * 预置常用计划模板，用户可一键添加到自己的计划列表。
 *
 * @param times 多个时间点，以逗号分隔，如 "07:00,12:30"
 * @param repeatDays 重复星期，如 "1,3,5"；为空表示每天
 */
@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val type: String,
    val times: String,
    val repeatDays: String = ""
)
