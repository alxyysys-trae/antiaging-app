package com.example.antiagingreminder.data

/**
 * 提醒计划类型枚举。
 * 用于区分抗衰运动、抗衰饮食、饮水、护肤、冥想等不同类别，
 * 并对应不同的主题色与统计分组。
 */
enum class PlanType(val label: String) {
    EXERCISE("抗衰运动"),
    DIET("抗衰饮食"),
    WATER("饮水"),
    SKINCARE("护肤"),
    MINDFULNESS("冥想"),
    OTHER("其他");

    companion object {
        /** 根据名称安全解析，找不到时返回 OTHER */
        fun fromName(name: String?): PlanType =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
