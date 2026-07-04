package com.example.antiagingreminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.antiagingreminder.MainActivity
import com.example.antiagingreminder.R

/**
 * 通知工具类。
 * 负责创建通知渠道与弹出系统通知，应用在后台或被杀死后仍可由广播触发显示。
 * 点击通知可直接打开应用并跳转到今日计划列表。
 *
 * ColorOS 16 适配：
 * - 设置高重要性 + 振动 + 亮屏 + 系统默认通知音，提升 ColorOS 通知到达率
 * - 设置 CATEGORY_REMINDER 分类，避免被系统归类为低优先级
 * - VISIBILITY_PRIVATE 隐藏锁屏健康隐私，仅显示精简版通知
 * - 使用唯一通知 ID（基于 planId + timeId 确定性映射），避免通知互相覆盖
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "anti_aging_channel"
        const val CHANNEL_NAME = "抗衰养生提醒"
        const val ACTION_OPEN_TODAY = "com.example.antiagingreminder.OPEN_TODAY"
        /** 通知 ID 基准偏移，避免与 planId 为 0 的通知冲突 */
        private const val NOTIF_ID_BASE = 10000
    }

    /** 创建通知渠道（Android 8.0+ 必需），重复创建无副作用 */
    fun createChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "抗衰养生与营养提醒通知"
            enableVibration(true)
            enableLights(true)
            // ColorOS 适配：使用系统默认通知音，确保有声音提醒
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)
            // ColorOS：不绕过免打扰，避免被系统强制静音
            setBypassDnd(false)
            // 锁屏隐藏完整内容，保护健康隐私
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * 弹出一条提醒通知。
     *
     * Bug 4 修复：通知 ID 同时包含 planId 和 timeId，
     * 避免同一计划的多个时间点通知互相覆盖。
     *
     * @param planId 计划 id
     * @param timeId 时间点 id（用于通知 id 唯一化）
     * @param title 通知标题（计划标题）
     * @param content 通知内容（计划描述）
     */
    fun showNotification(planId: Long, timeId: Long, title: String, content: String) {
        // 点击后打开 MainActivity，携带跳转今日列表的 action
        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_TODAY
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            planId.toInt(),
            openIntent,
            // ColorOS 适配：使用 FLAG_UPDATE_CURRENT 确保跳转正确
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // ColorOS 适配：设置通知分类为提醒，提升通知可见性
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setColor(ContextCompat.getColor(context, R.color.seed))
            // 锁屏隐藏完整内容，仅显示精简版通知保护健康隐私
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("抗衰提醒")
                    .setContentText("您有一条健康提醒，点击查看详情")
                    .setColor(ContextCompat.getColor(context, R.color.seed))
                    .setAutoCancel(true)
                    .build()
            )
            // ColorOS 适配：设置 ticker，部分系统通知中心会读取
            .setTicker(title)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        // 通知 id 使用与 requestCode 一致的确定性映射，避免哈希碰撞
        manager.notify(notificationId(planId, timeId), notification)
    }

    /** 通知 ID：与 AlarmSchedulerImpl.requestCode 一致的确定性映射，避免碰撞 */
    private fun notificationId(planId: Long, timeId: Long): Int =
        NOTIF_ID_BASE + ((planId.toInt() and 0xFFFF) shl 16 or (timeId.toInt() and 0xFFFF))
}
