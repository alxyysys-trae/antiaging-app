package com.example.antiagingreminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 浅色配色方案：柔和清新，符合养生主题
private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    tertiary = Tertiary,
    tertiaryContainer = TertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    outline = Outline
)

// 深色配色方案
private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = OnPrimaryContainer,
    onPrimaryContainer = PrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = Color(0xFF14171A),
    onBackground = Color(0xFFE4E6E8),
    surface = Color(0xFF1C2024),
    onSurface = Color(0xFFE4E6E8)
)

/**
 * 应用主题。
 * 默认使用浅色方案（养生清新），暗色模式下自动切换。
 */
@Composable
fun AntiAgingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
