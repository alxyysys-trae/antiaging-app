package com.example.antiagingreminder.ui.templates

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.data.local.entity.TemplateEntity
import com.example.antiagingreminder.ui.common.PillLabel
import com.example.antiagingreminder.ui.common.color
import com.example.antiagingreminder.util.DateTimeUtils

/**
 * 模板库界面。
 * 预置常用抗衰养生计划模板，用户可一键添加到自己的计划列表，
 * 添加后仍可自由修改标题、描述、时间、重复规则或删除。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel,
    onBack: () -> Unit,
    onAddNewPlan: () -> Unit
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var addedTitle by remember { mutableStateOf<String?>(null) }

    // 添加成功后弹出提示
    LaunchedEffect(addedTitle) {
        addedTitle?.let {
            snackbarHostState.showSnackbar("已添加「$it」，可在计划中修改")
            addedTitle = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模板库", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onAddNewPlan) {
                        Icon(Icons.Filled.Add, contentDescription = "新建计划")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 说明卡
            item {
                Text(
                    "一键添加常用计划，添加后可自由修改时间、重复与内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            items(templates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onAdd = {
                        viewModel.addFromTemplate(it) { _ ->
                            addedTitle = template.title
                        }
                    }
                )
            }
        }
    }
}

/** 单个模板卡片 */
@Composable
private fun TemplateCard(template: TemplateEntity, onAdd: (TemplateEntity) -> Unit) {
    val type = PlanType.fromName(template.type)
    val accent = type.color()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAdd(template) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧类型色块图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    PillLabel(text = type.label, color = accent)
                }
                if (template.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        maxLines = 2
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "提醒时间：${template.times} · ${repeatText(template.repeatDays)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.width(8.dp))
            // 添加按钮
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .clickable { onAdd(template) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加", tint = Color.White)
            }
        }
    }
}

/** 重复规则可读化 */
private fun repeatText(repeatDays: String): String {
    val days = DateTimeUtils.parseDays(repeatDays)
    if (days.isEmpty()) return "每天"
    val names = mapOf(
        1 to "一", 2 to "二", 3 to "三", 4 to "四",
        5 to "五", 6 to "六", 7 to "日"
    )
    return "每周" + days.sorted().joinToString("、") { names[it] ?: "" }
}
