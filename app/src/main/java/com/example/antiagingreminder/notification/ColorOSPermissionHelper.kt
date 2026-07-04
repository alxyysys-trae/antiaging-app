package com.example.antiagingreminder.notification

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * ColorOS / 国产 ROM 权限引导工具。
 *
 * ColorOS 16 有非常严格的后台管理，需引导用户开启以下权限才能保证提醒准时到达：
 * 1. 自启动管理（开机后可自动运行）
 * 2. 电池优化白名单（防止后台被冻结）
 * 3. 后台弹出界面（部分 ColorOS 版本需要）
 *
 * 由于各厂商 ROM 的设置页包名不统一，这里通过多方案 Intent 逐个尝试。
 */
object ColorOSPermissionHelper {

    /** 检查是否已加入电池优化白名单 */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** 跳转电池优化设置（请求加入白名单） */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${context.packageName}"))
                context.startActivity(intent)
            } catch (_: Exception) {
                // 回退到电池优化列表页
                try {
                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                } catch (_: Exception) { /* 忽略 */ }
            }
        }
    }

    /**
     * 跳转 ColorOS 自启动管理页。
     * ColorOS 的自启动设置页包名因版本而异，逐个尝试。
     */
    fun openAutoStartSettings(context: Context): Boolean {
        val intents = listOf(
            // ColorOS 13/14/16 自启动管理
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            // OPPO 安全中心
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            // 一加（OnePlus）自启动
            Intent().setComponent(
                ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainstart.view.ChainStartAppListActivity"
                )
            ),
            // 通用安全中心
            Intent().setComponent(
                ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$HighPowerUsageActivity"
                )
            )
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // 尝试下一个方案
            }
        }
        return false
    }

    /**
     * 跳转应用详情页（作为兜底，让用户手动找权限设置）。
     */
    fun openAppDetailSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) { /* 忽略 */ }
    }

    /**
     * 检查是否为 OPPO/一加/realme 设备（ColorOS 系统）。
     */
    fun isColorOS(): Boolean {
        val brand = Build.BRAND?.lowercase() ?: ""
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        return brand.contains("oppo") ||
               brand.contains("oneplus") ||
               brand.contains("realme") ||
               manufacturer.contains("oppo") ||
               manufacturer.contains("oneplus")
    }
}
