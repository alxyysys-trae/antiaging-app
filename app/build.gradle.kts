plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.antiagingreminder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.antiagingreminder"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // 支持多 dex（兼容低版本设备）
        multiDexEnabled = true
    }

    // 签名配置由 build.gradle.kts 顶层 signingConfigs 块处理
    // CI 环境通过环境变量传入 keystore 路径，本地使用 debug 签名
    buildTypes {
        debug {
            // debug 使用默认签名
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // release 签名由 CI 环境的 keystore 注入
            // 如果没有配置 keystore，则生成 unsigned APK（也可安装）
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // Compose BOM 统一管理 Compose 版本
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose UI 与 Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")

    // 导航
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room 本地数据库（纯本地存储，无需网络）
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager（ColorOS 兜底保活，周期性补注册闹钟，无需网络）
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // androidx.startup（WorkManager 自动初始化依赖）
    implementation("androidx.startup:startup-runtime:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
