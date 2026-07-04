package com.example.antiagingreminder

import android.app.Application
import com.example.antiagingreminder.di.AppContainer
import com.example.antiagingreminder.notification.AlarmFallbackWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 应用入口。
 * 在此创建依赖容器、初始化通知渠道，并在启动时排程一次全部提醒，
 * 保证应用安装或启动后提醒能准时触发。
 *
 * ColorOS 适配：
 * - 启动时注册 WorkManager 兜底任务，周期性补注册闹钟
 * - 防止 ColorOS 深度冻结导致闹钟丢失
 */
class AntiAgingApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // 创建通知渠道
        container.initNotifications()
        // 启动时统一排程所有提醒（添加异常保护，避免启动崩溃）
        appScope.launch {
            try {
                container.alarmScheduler.rescheduleAll()
            } catch (e: Exception) {
                // 静默处理，兜底 Worker 会重试
            }
        }
        // ColorOS 适配：注册 WorkManager 兜底任务，周期性补注册闹钟
        try {
            AlarmFallbackWorker.schedule(this)
        } catch (e: Exception) {
            // WorkManager 初始化失败不阻塞应用启动
        }
    }
}
