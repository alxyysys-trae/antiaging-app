package com.example.antiagingreminder.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.ui.theme.ColorDiet
import com.example.antiagingreminder.ui.theme.ColorExercise
import com.example.antiagingreminder.ui.theme.ColorMindfulness
import com.example.antiagingreminder.ui.theme.ColorOther
import com.example.antiagingreminder.ui.theme.ColorSkincare
import com.example.antiagingreminder.ui.theme.ColorWater
import com.example.antiagingreminder.ui.theme.EveningBottom
import com.example.antiagingreminder.ui.theme.EveningTop
import com.example.antiagingreminder.ui.theme.MorningBottom
import com.example.antiagingreminder.ui.theme.MorningTop
import com.example.antiagingreminder.ui.theme.NightBottom
import com.example.antiagingreminder.ui.theme.NightTop
import com.example.antiagingreminder.ui.theme.NoonBottom
import com.example.antiagingreminder.ui.theme.NoonTop

/** 计划类型对应主题色 */
fun PlanType.color(): Color = when (this) {
    PlanType.EXERCISE -> ColorExercise
    PlanType.DIET -> ColorDiet
    PlanType.WATER -> ColorWater
    PlanType.SKINCARE -> ColorSkincare
    PlanType.MINDFULNESS -> ColorMindfulness
    PlanType.OTHER -> ColorOther
}

/** 根据小时返回对应时间段的渐变色（早/中/晚/夜） */
fun timeGradient(hour: Int): List<Color> = when (hour) {
    in 5..10 -> listOf(MorningTop, MorningBottom)   // 清晨：暖橘渐变
    in 11..15 -> listOf(NoonTop, NoonBottom)         // 午间：清新绿
    in 16..19 -> listOf(EveningTop, EveningBottom)   // 傍晚：柔粉紫
    else -> listOf(NightTop, NightBottom)            // 夜晚：静谧蓝
}

/** 时段名称 */
fun periodLabel(hour: Int): String = when (hour) {
    in 5..10 -> "清晨"
    in 11..15 -> "午间"
    in 16..19 -> "傍晚"
    else -> "夜晚"
}

/**
 * 时段渐变背景容器。
 * 根据当前时间在早中晚夜之间过渡，营造一天时间流转的氛围。
 */
@Composable
fun TimeGradientBackground(
    hour: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = timeGradient(hour)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) { content() }
}

/** 圆角标签（用于类型徽标等） */
@Composable
fun PillLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
