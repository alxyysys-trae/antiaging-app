package com.example.antiagingreminder.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antiagingreminder.domain.TimelineItem
import com.example.antiagingreminder.ui.common.PillLabel
import com.example.antiagingreminder.ui.common.color
import com.example.antiagingreminder.util.DateTimeUtils
import java.util.Calendar

/**
 * 历史记录界面。
 * 顶部提供最近日期切换条，下方按日期查看过往提醒完成情况与统计。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    // 最近 14 天日期条（基于今天计算，与 selectedDate 无关，避免不必要重计算）
    val recentDates = remember { lastNDates(14) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 日期选择条
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recentDates, key = { it }) { date ->
                    DateChip(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { viewModel.selectDate(date) }
                    )
                }
            }

            val (total, done, progress) = viewModel.statsFor(items)
            // 当日统计卡
            StatsSummary(date = selectedDate, done = done, total = total, progress = progress)

            // 当日条目列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { "${it.planId}_${it.reminderTimeId}" }) { item ->
                    HistoryCard(item = item)
                }
            }
        }
    }
}

/** 日期切换芯片 */
@Composable
private fun DateChip(date: String, isSelected: Boolean, onClick: () -> Unit) {
    val cal = Calendar.getInstance().apply { time = DateTimeUtils.stringToDate(date) }
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val weekday = arrayOf("日", "一", "二", "三", "四", "五", "六")[cal.get(Calendar.DAY_OF_WEEK) - 1]

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "${day}日",
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            "周$weekday",
            fontSize = 11.sp,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/** 当日完成统计摘要 */
@Composable
private fun StatsSummary(date: String, done: Int, total: Int, progress: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$date 完成情况", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$done / $total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (total == 0) "暂无提醒" else "${(progress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/** 历史条目卡片 */
@Composable
private fun HistoryCard(item: TimelineItem) {
    val accent = item.type.color()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态圆
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (item.isCompleted) accent else Color.Transparent)
                    .border(
                        width = if (item.isCompleted) 0.dp else 1.5.dp,
                        color = if (item.isCompleted) Color.Transparent else accent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCompleted) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.timeText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(Modifier.width(8.dp))
                    PillLabel(text = item.type.label, color = accent)
                }
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alpha(if (item.isCompleted) 0.6f else 1f)
                )
            }
        }
    }
}

/** 计算最近 n 天日期字符串列表（含今天，倒序） */
private fun lastNDates(n: Int): List<String> {
    val cal = Calendar.getInstance()
    return (0 until n).map { offset ->
        val c = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
        DateTimeUtils.dateToString(c.time)
    }
}
