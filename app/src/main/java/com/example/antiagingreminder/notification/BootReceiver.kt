package com.example.antiagingreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.antiagingreminder.AntiAgingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 开机 / 时间变更 / 应用更新后接收广播，重新注册全部提醒。
 * 设备重启后 AlarmManager 中已注册的闹钟会丢失，因此需在此补注册。
 *
 * ColorOS 适配：
 * - 同时重新注册 WorkManager 兜底任务
 * - ColorOS 默认禁止开机自启动，需用户在引导中开启自启动权限
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(8_000) {
                    val container = (context.applicationContext as? AntiAgingApp)?.container
                    // 确保通知渠道存在
                    container?.notificationHelper?.createChannel()
                        ?: NotificationHelper(context).createChannel()
                    // 重新排程全部闹钟（复用单例）
                    (container?.alarmScheduler ?: AlarmSchedulerImpl(context)).rescheduleAll()
                    // ColorOS：重新注册 WorkManager 兜底任务
                    AlarmFallbackWorker.schedule(context)
                }
            } catch (e: Exception) {
                // 静默吞掉异常，防止开机崩溃；兜底 Worker 会在后续重试
            } finally {
                try { pending.finish() } catch (_: Exception) {}
            }
        }
    }
}
