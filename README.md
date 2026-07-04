# 抗衰提醒 Android 应用 — 在线打包说明

## 项目信息
- **应用名**: 抗衰提醒 (AntiAgingReminder)
- **包名**: com.example.antiagingreminder
- **最低 SDK**: 26 (Android 8.0)
- **目标 SDK**: 34 (Android 14)
- **语言**: Kotlin + Jetpack Compose
- **架构**: MVVM + Room + Flow
- **网络**: 无需任何网络权限，完全本地运行

## 在线打包方式

### 方式一：GitHub Actions（推荐）
1. 将整个 `AntiAgingReminder/` 目录上传到 GitHub 仓库
2. 添加 `.github/workflows/build.yml`（见下方）
3. Push 后自动编译，在 Actions 页面下载 APK

### 方式二：Web 在线构建服务
1. 将 `AntiAgingReminder/` 目录打包为 ZIP
2. 上传到在线 Android 构建平台（如 https://apk-builder.github.io 等）
3. 平台会自动编译并返回 APK

### 方式三：本地构建
```bash
cd AntiAgingReminder
./gradlew assembleDebug    # 生成 debug APK
./gradlew assembleRelease  # 生成 release APK（使用 debug 签名）
```
生成的 APK 位于 `app/build/outputs/apk/debug/` 或 `app/build/outputs/apk/release/`

## GitHub Actions 配置示例
```yaml
name: Build APK
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Setup Android SDK
        uses: android-actions/setup-android@v3
      - name: Build Debug APK
        working-directory: ./AntiAgingReminder
        run: ./gradlew assembleDebug --no-daemon
      - uses: actions/upload-artifact@v4
        with:
          name: app-debug.apk
          path: AntiAgingReminder/app/build/outputs/apk/debug/*.apk
```

## 权限说明（全部本地，无网络）
| 权限 | 用途 |
|------|------|
| POST_NOTIFICATIONS | 弹出提醒通知 (Android 13+) |
| SCHEDULE_EXACT_ALARM | 精确闹钟，保证提醒准时 |
| USE_EXACT_ALARM | 精确闹钟（目标 SDK 33+） |
| RECEIVE_BOOT_COMPLETED | 开机后重新注册提醒 |
| VIBRATE | 通知振动 |

## 通知功能验证
- AlarmManager + BroadcastReceiver 确保后台/被杀后仍可触发
- BootReceiver 在设备重启后重新注册所有闹钟
- NotificationChannel 在 Application 启动时创建
- 点击通知跳转到今日计划列表
- 支持按星期循环提醒，自动计算下一次触发时间
