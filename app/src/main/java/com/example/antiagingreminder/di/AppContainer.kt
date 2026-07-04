package com.example.antiagingreminder.di

import android.content.Context
import com.example.antiagingreminder.data.local.AppDatabase
import com.example.antiagingreminder.data.repository.PlanRepository
import com.example.antiagingreminder.notification.AlarmScheduler
import com.example.antiagingreminder.notification.AlarmSchedulerImpl
import com.example.antiagingreminder.notification.NotificationHelper

/**
 * 简易依赖容器（手动依赖注入，避免引入 Hilt 增加复杂度）。
 * 在 Application 中创建单例，界面层通过该容器获取仓库与 ViewModel 工厂。
 */
class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy { AppDatabase.get(context) }

    val alarmScheduler: AlarmScheduler by lazy { AlarmSchedulerImpl(context) }

    val notificationHelper: NotificationHelper by lazy { NotificationHelper(context) }

    val repository: PlanRepository by lazy {
        PlanRepository(
            planDao = database.planDao(),
            historyDao = database.historyDao(),
            templateDao = database.templateDao(),
            alarmScheduler = alarmScheduler
        )
    }

    /** 初始化通知渠道与初始提醒排程 */
    fun initNotifications() {
        notificationHelper.createChannel()
    }
}
