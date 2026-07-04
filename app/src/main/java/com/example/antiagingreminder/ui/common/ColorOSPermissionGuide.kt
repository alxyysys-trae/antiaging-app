package com.example.antiagingreminder.ui.common

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiagingreminder.notification.ColorOSPermissionHelper

/**
 * ColorOS 权限引导卡片。
 *
 * ColorOS 16 默认禁止应用自启动和后台运行，需引导用户开启以下权限，
 * 否则应用被杀后无法收到提醒通知：
 * 1. 电池优化白名单
 * 2. 自启动管理
 *
 * 修复：自启动状态无法被 API 检测，改为用户手动确认「我已开启」后隐藏。
 * 电池优化状态可通过 API 检测，自动刷新。
 */
@Composable
fun ColorOSPermissionGuide() {
    val context = LocalContext.current
    // 电池优化可通过 API 检测
    var showBatteryOptimized by remember {
        mutableStateOf(!ColorOSPermissionHelper.isIgnoringBatteryOptimizations(context))
    }
    // 从系统设置返回后自动刷新电池优化状态
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                showBatteryOptimized = !ColorOSPermissionHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // 自启动无法通过 API 检测，用 remember 持久化用户确认状态
    // 使用 SharedPreferences 持久化，避免重启后重复弹出
    val prefs = remember { context.getSharedPreferences("coloros_perms", android.content.Context.MODE_PRIVATE) }
    var autoStartConfirmed by remember {
        mutableStateOf(
            ColorOSPermissionHelper.isColorOS() && !prefs.getBoolean("autostart_confirmed", false)
        )
    }
    var showDetailDialog by remember { mutableStateOf(false) }

    // 两项都已处理则不显示卡片
    if (!showBatteryOptimized && !autoStartConfirmed) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable { showDetailDialog = true }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "开启后台保活，确保提醒不丢失",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    buildPermissionHint(showBatteryOptimized, autoStartConfirmed),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Icon(
                Icons.Filled.Shield,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // 详细引导对话框
    if (showDetailDialog) {
        PermissionGuideDialog(
            showBattery = showBatteryOptimized,
            showAutoStart = autoStartConfirmed,
            onBatteryClick = {
                ColorOSPermissionHelper.requestIgnoreBatteryOptimizations(context)
            },
            onAutoStartClick = {
                val opened = ColorOSPermissionHelper.openAutoStartSettings(context)
                if (!opened) {
                    ColorOSPermissionHelper.openAppDetailSettings(context)
                    Toast.makeText(context, "请在应用设置中开启自启动权限", Toast.LENGTH_LONG).show()
                }
            },
            onDone = {
                // 重新检测电池优化状态
                showBatteryOptimized = !ColorOSPermissionHelper.isIgnoringBatteryOptimizations(context)
                // 自启动无法检测，用户确认后持久化并隐藏
                if (autoStartConfirmed) {
                    prefs.edit().putBoolean("autostart_confirmed", true).apply()
                    autoStartConfirmed = false
                }
                showDetailDialog = false
            },
            onDismiss = { showDetailDialog = false }
        )
    }
}

/** 构建权限提示文案 */
private fun buildPermissionHint(battery: Boolean, autoStart: Boolean): String {
    val parts = mutableListOf<String>()
    if (battery) parts.add("电池优化白名单")
    if (autoStart) parts.add("自启动管理")
    return if (parts.isEmpty()) "已全部开启" else "待开启：${parts.joinToString("、")}"
}

/** 权限引导详细对话框 */
@Composable
private fun PermissionGuideDialog(
    showBattery: Boolean,
    showAutoStart: Boolean,
    onBatteryClick: () -> Unit,
    onAutoStartClick: () -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保障提醒准时到达", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "为确保应用在后台或被关闭后仍能准时弹出提醒，请开启以下权限：",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (showBattery) {
                    PermissionStep(
                        num = 1,
                        title = "电池优化白名单",
                        desc = "防止系统冻结应用，保证后台闹钟运行",
                        onClick = onBatteryClick
                    )
                }
                if (showAutoStart) {
                    PermissionStep(
                        num = if (showBattery) 2 else 1,
                        title = "自启动管理",
                        desc = "开机后自动运行应用，重新注册提醒",
                        onClick = onAutoStartClick
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDone) { Text("我已开启", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("稍后") }
        }
    )
}

/** 单个权限步骤 */
@Composable
private fun PermissionStep(num: Int, title: String, desc: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$num", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(desc, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Text("去开启", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
