package com.example.antiagingreminder.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 时间与日期工具函数集合。
 * 集中处理日期格式化、星期判断、时间点解析，便于各层复用。
 *
 * 修复：SimpleDateFormat 非线程安全，多协程并发调用会崩溃。
 * 改为每次调用创建新实例（开销极小），避免线程安全问题。
 */
object DateTimeUtils {

    /** 返回今天的日期字符串 yyyy-MM-dd */
    fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    /** 将 Date 转为日期字符串 */
    fun dateToString(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

    /** 将日期字符串解析为 Date */
    fun stringToDate(date: String): Date =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()

    /** 获取当前时间的总分钟数 */
    fun nowMinutes(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    }

    /**
     * 返回 ISO 星期几：1=周一 … 7=周日。
     * 注意 Android Calendar 中 Sunday=1，需转换为 ISO。
     */
    fun isoDayOfWeek(): Int {
        val c = Calendar.getInstance()
        val day = c.get(Calendar.DAY_OF_WEEK) // Sunday=1 ... Saturday=7
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }

    /**
     * 计算指定日期字符串对应的 ISO 星期（1=周一…7=周日）。
     * 统一供 PlanRepository 和 HistoryViewModel 使用，避免逻辑重复。
     */
    fun isoDayOfWeekForDate(date: String): Int {
        val d = stringToDate(date)
        val c = Calendar.getInstance().apply { time = d }
        val day = c.get(Calendar.DAY_OF_WEEK)
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }

    /** 将 "1,3,5" 解析为 [1,3,5]；空串返回空集合 */
    fun parseDays(days: String): Set<Int> =
        days.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

    /** 将 [1,3,5] 转为 "1,3,5" */
    fun daysToString(days: Set<Int>): String =
        days.sorted().joinToString(",")

    /** 将 "07:00,12:30" 解析为时间点列表 */
    fun parseTimes(times: String): List<Pair<Int, Int>> =
        times.split(",").mapNotNull { token ->
            val parts = token.trim().split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull()
                val m = parts[1].toIntOrNull()
                if (h != null && m != null && h in 0..23 && m in 0..59) h to m else null
            } else null
        }

    /** 格式化当前时间为 HH:mm */
    fun nowTimeText(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
