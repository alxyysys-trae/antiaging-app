package com.example.antiagingreminder.notification

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.antiagingreminder.AntiAgingApp
import java.util.concurrent.TimeUnit

/**
 * 闹钟兜底 Worker。
 *
 * ColorOS 16 会积极冻结后台进程，AlarmManager 注册的闹钟在某些情况下可能丢失
 * （如系统深度休眠、应用被强杀后）。此 Worker 每 30 分钟周期性运行，
 * 重新排程全部闹钟，确保提醒不会因系统限制而丢失。
 *
 * 注意：WorkManager 不能保证精确时间，仅作为兜底补充机制。
 * 精确提醒仍依赖 AlarmManager 的精确闹钟。
 */
class AlarmFallbackWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 复用 AppContainer 单例，避免重复创建
            val container = (applicationContext as? AntiAgingApp)?.container
            (container?.alarmScheduler ?: AlarmSchedulerImpl(applicationContext)).rescheduleAll()
            Result.success()
        } catch (e: Exception) {
            // 失败时 WorkManager 会按退避策略重试
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "alarm_fallback_worker"

        /**
         * 注册周期性兜底任务。
         * 每 30 分钟运行一次，检查并补注册闹钟。
         * 使用 KEEP 策略避免重复创建。
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AlarmFallbackWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        // 不需要网络（断网也能运行）
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                // 指数退避重试，避免频繁重试耗电
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /** 取消兜底任务 */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
