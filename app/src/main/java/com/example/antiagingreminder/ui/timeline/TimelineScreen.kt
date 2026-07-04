package com.example.antiagingreminder.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antiagingreminder.domain.TimelineItem
import com.example.antiagingreminder.ui.common.ColorOSPermissionGuide
import com.example.antiagingreminder.ui.common.PillLabel
import com.example.antiagingreminder.ui.common.TimeGradientBackground
import com.example.antiagingreminder.ui.common.color
import com.example.antiagingreminder.ui.common.periodLabel
import com.example.antiagingreminder.util.DateTimeUtils

/**
 * 今日时间轴主界面。
 * 以时间轴从早到晚排列卡片，左侧贯穿轴线与「现在」指示线，
 * 背景按当前时段渐变，已过时间卡片变灰，并展示今日完成统计卡。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel,
    onOpenTemplates: () -> Unit,
    onOpenHistory: () -> Unit,
    onAddPlan: () -> Unit,
    onEditPlan: (Long) -> Unit
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val nowMinutes by viewModel.nowMinutes.collectAsStateWithLifecycle()

    val currentHour = nowMinutes / 60
    val dateText = DateTimeUtils.today()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("抗衰提醒", fontWeight = FontWeight.SemiBold)
                        Text(
                            "$dateText · ${periodLabel(currentHour)} · 抗衰进度 ${stats.overall.done}/${stats.overall.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTemplates) {
                        Icon(Icons.Filled.MenuBook, contentDescription = "模板库")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Filled.History, contentDescription = "历史记录")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(
                onClick = onAddPlan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Outlined.Add, contentDescription = "新建计划") }
        }
    ) { padding ->
        TimeGradientBackground(hour = currentHour, modifier = Modifier.padding(padding)) {
            if (items.isEmpty()) {
                EmptyTimeline(onAddPlan = onAddPlan, onOpenTemplates = onOpenTemplates)
            } else {
                TimelineList(
                    items = items,
                    date = dateText,
                    nowMinutes = nowMinutes,
                    stats = stats,
                    onToggle = { viewModel.toggleComplete(it) },
                    onEdit = onEditPlan
                )
            }
        }
    }
}

/**
 * 时间轴列表：统计卡 + 按时间排列的卡片 + 「现在」指示线。
 * 首次加载时自动滚动，使「现在」指示线停留在屏幕上方的默认位置。
 */
@Composable
private fun TimelineList(
    items: List<TimelineItem>,
    date: String,
    nowMinutes: Int,
    stats: TodayStats,
    onToggle: (TimelineItem) -> Unit,
    onEdit: (Long) -> Unit
) {
    // 计算首个「未来」条目索引（用于高亮与插入「现在」指示线）
    val nextIndex = items.indexOfFirst { it.minutesOfDay > nowMinutes }
    val nowInsertIndex = if (nextIndex == -1) items.size else nextIndex

    val listState: LazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val defaultOffsetPx = with(density) { 120.dp.toPx() }.toInt()

    // 仅在数据就绪后自动定位到「现在」，跨天时（date 变化）重新触发
    LaunchedEffect(items.isNotEmpty(), date) {
        if (items.isNotEmpty()) {
            val target = (nowInsertIndex + 1).coerceAtMost(items.size) // +1 偏移统计卡
            listState.scrollToItem(target, defaultOffsetPx)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 今日完成统计卡
        item(key = "stats_card") { StatsCard(stats) }

        // ColorOS 权限引导卡（仅在需要时显示）
        item(key = "permission_guide") { ColorOSPermissionGuide() }

        // 时间轴卡片（含「现在」指示线）
        itemsIndexed(items, nowInsertIndex) { item, index ->
            TimelineCardRow(
                item = item,
                isPast = item.minutesOfDay <= nowMinutes,
                isNext = index == nextIndex,
                onToggle = { onToggle(item) },
                onEdit = { onEdit(item.planId) }
            )
        }
    }
}

/** 在 items 列表中按位置插入「现在」指示线，使用 key 防止状态错乱 */
private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexed(
    items: List<TimelineItem>,
    nowInsertIndex: Int,
    row: @Composable (TimelineItem, Int) -> Unit
) {
    items.forEachIndexed { index, item ->
        if (index == nowInsertIndex) item(key = "now_indicator_${index}") { NowIndicator() }
        item(key = "item_${item.planId}_${item.reminderTimeId}") { row(item, index) }
    }
    if (nowInsertIndex >= items.size) item(key = "now_indicator_end") { NowIndicator() }
}

/** 「现在 HH:mm」指示线 */
@Composable
private fun NowIndicator() {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .background(accent.copy(alpha = 0.5f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "现在 ${DateTimeUtils.nowTimeText()}",
            color = accent,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * 单条时间轴卡片。
 * 左侧为时间轴（时间文本 + 节点 + 连接线），右侧为提醒卡片内容。
 */
@Composable
private fun TimelineCardRow(
    item: TimelineItem,
    isPast: Boolean,
    isNext: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    val accent = item.type.color()
    val axisColor = if (isPast) MaterialTheme.colorScheme.outline else accent

    Row(modifier = Modifier.fillMaxWidth().clickable { onEdit() }) {
        // 左侧时间轴：时间文本 + 节点 + 连接竖线
        Box(
            modifier = Modifier.width(64.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // 贯穿整行的连接竖线
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .background(axisColor.copy(alpha = 0.4f))
            )
            Column(
                modifier = Modifier.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.timeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPast) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                // 节点圆点
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (item.isCompleted) accent else Color.White)
                        .then(
                            Modifier
                                .clip(CircleShape)
                                .background(axisColor.copy(alpha = if (item.isCompleted) 1f else 0.25f))
                        )
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // 右侧卡片
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPast) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        PillLabel(text = item.type.label, color = accent)
                    }
                    if (item.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isPast) 0.4f else 0.65f),
                            maxLines = 3
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                // 完成勾选
                CircleCheckbox(
                    checked = item.isCompleted,
                    color = accent,
                    onChecked = onToggle
                )
            }
        }
    }
}

/** 圆形勾选框（苹果风格）：选中为实心圆 + 对勾，未选中为空心圆 */
@Composable
private fun CircleCheckbox(checked: Boolean, color: Color, onChecked: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (checked) color else Color.Transparent)
            .border(
                width = if (checked) 0.dp else 1.5.dp,
                color = if (checked) Color.Transparent else color,
                shape = CircleShape
            )
            .clickable { onChecked() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "已完成",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 今日完成统计卡：展示运动、饮食、饮水的完成进度。
 */
@Composable
private fun StatsCard(stats: TodayStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("今日抗衰任务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(
                    "${stats.overall.done}/${stats.overall.total}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(14.dp))
            StatRow("运动", stats.exercise)
            Spacer(Modifier.height(8.dp))
            StatRow("饮食", stats.diet)
            Spacer(Modifier.height(8.dp))
            StatRow("饮水", stats.water)
            Spacer(Modifier.height(8.dp))
            StatRow("护肤", stats.skincare)
            Spacer(Modifier.height(8.dp))
            StatRow("冥想", stats.mindfulness)
            Spacer(Modifier.height(8.dp))
            // 整体进度条
            LinearProgressIndicator(
                progress = { stats.overall.progress },
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

/** 单行统计：类型 + 进度条 + 数量 */
@Composable
private fun StatRow(label: String, stat: CompletionStat) {
    val color = stat.type.color()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(40.dp))
        LinearProgressIndicator(
            progress = { stat.progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${stat.done}/${stat.total}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(40.dp)
        )
    }
}

/** 空状态：引导用户添加计划或从模板添加 */
@Composable
private fun EmptyTimeline(onAddPlan: () -> Unit, onOpenTemplates: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("今天还没有提醒计划", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "从模板库一键添加科学抗衰方案，或新建自定义提醒",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(20.dp))
        androidx.compose.material3.Button(onClick = onOpenTemplates) {
            Icon(Icons.Filled.MenuBook, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("从模板添加")
        }
    }
}
