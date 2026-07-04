package com.example.antiagingreminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.antiagingreminder.ui.navigation.AppNavigation
import com.example.antiagingreminder.ui.theme.AntiAgingTheme

/**
 * 主 Activity。
 * 负责申请通知权限与精确闹钟权限，并承载 Compose 导航。
 * 点击通知后通过 OPEN_TODAY action 回到今日计划列表。
 *
 * ColorOS 适配：权限申请全部延迟到 Compose 内异步处理，
 * 避免每次启动都弹出系统设置页（原实现会在 onCreate 中直接跳转）。
 */
class MainActivity : ComponentActivity() {

    /** 通知点击时传递的 action，用于触发导航到今日页 */
    private var pendingAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as AntiAgingApp).container
        pendingAction = intent?.action

        setContent {
            AntiAgingTheme {
                val notifLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(container = container, pendingAction = pendingAction)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingAction = intent.action
    }
}
