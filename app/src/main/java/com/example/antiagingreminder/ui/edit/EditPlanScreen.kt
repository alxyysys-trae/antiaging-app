package com.example.antiagingreminder.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.ui.common.color

/** 星期定义（1=周一…7=周日） */
private val WeekDays = listOf(
    1 to "一", 2 to "二", 3 to "三", 4 to "四",
    5 to "五", 6 to "六", 7 to "日"
)

/**
 * 新建/编辑计划界面。
 * 支持自定义标题、描述、类型、重复星期与多个提醒时间点，
 * 保存后自动重新排程通知；编辑已有计划时可删除。
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditPlanScreen(
    viewModel: EditPlanViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 保存成功后退出
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    // 时间选择对话框状态
    var editingTime by remember { mutableStateOf<EditTime?>(null) }
    // 删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id > 0) "编辑计划" else "新建计划",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = state.isValid
                    ) { Text("保存", fontWeight = FontWeight.SemiBold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("加载中…", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            SectionCard("标题") {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("如：晨间面部瑜伽") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // 详细描述
            SectionCard("详细描述（动作说明 / 食谱 / 饮水量）") {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("如：起床后饮用 300ml 温水，促进代谢") },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // 类型选择
            SectionCard("类型") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanType.entries.forEach { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            label = { Text(type.label) },
                            leadingIcon = {
                                Box(
                                    Modifier.size(10.dp).clip(CircleShape).background(type.color())
                                )
                            }
                        )
                    }
                }
            }

            // 启用/禁用
            SectionCard("启用状态") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (state.isActive) "已启用（将按时提醒）" else "已暂停（不提醒）",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = state.isActive,
                        onCheckedChange = { viewModel.onActiveChange(it) }
                    )
                }
            }

            // 重复星期
            SectionCard("重复（不选则每天）") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeekDays.forEach { (day, name) ->
                        AssistChip(
                            onClick = { viewModel.toggleDay(day) },
                            label = { Text(name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (day in state.repeatDays) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (day in state.repeatDays) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // 提醒时间点
            SectionCard(
                title = "提醒时间点",
                trailing = {
                    IconButton(onClick = viewModel::addTime) {
                        Icon(Icons.Filled.Add, contentDescription = "添加时间")
                    }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 按时间排序显示，避免添加顺序导致乱序
                    val sortedTimes = state.times.sortedBy { it.hour * 60 + it.minute }
                    sortedTimes.forEach { time ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "%02d:%02d".format(time.hour, time.minute),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { editingTime = time }
                                    .padding(vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { viewModel.removeTime(time.tempId) }) {
                                Icon(Icons.Filled.Close, contentDescription = "删除时间点")
                            }
                        }
                    }
                    if (state.times.isEmpty()) {
                        Text(
                            "至少添加一个提醒时间点",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 保存按钮
            Button(
                onClick = { viewModel.save() },
                enabled = state.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("保存计划", fontWeight = FontWeight.SemiBold) }

            // 删除按钮（仅编辑已有计划）
            if (state.id > 0) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("删除该计划")
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除后无法恢复，确定要删除该计划吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.delete()
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }

    // 时间选择对话框
    editingTime?.let { time ->
        TimePickerDialog(
            initialHour = time.hour,
            initialMinute = time.minute,
            onConfirm = { h, m ->
                viewModel.updateTime(time.tempId, h, m)
                editingTime = null
            },
            onDismiss = { editingTime = null }
        )
    }
}

/** 区块卡片容器 */
@Composable
private fun SectionCard(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/** 时间选择对话框（基于 Material3 TimePicker） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        text = { TimePicker(state = state) }
    )
}
