package com.example.antiagingreminder.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.data.repository.PlanRepository
import com.example.antiagingreminder.di.AppContainer
import com.example.antiagingreminder.domain.TimelineItem
import com.example.antiagingreminder.ui.VmFactory
import com.example.antiagingreminder.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/** 单个类型的完成统计 */
data class CompletionStat(val type: PlanType, val total: Int, val done: Int) {
    val progress: Float get() = if (total == 0) 0f else done.toFloat() / total
    val finished: Boolean get() = total > 0 && done == total
}

/** 今日整体统计（覆盖全部计划类型） */
data class TodayStats(
    val exercise: CompletionStat,
    val diet: CompletionStat,
    val water: CompletionStat,
    val skincare: CompletionStat,
    val mindfulness: CompletionStat,
    val overall: CompletionStat
)

/**
 * 今日时间轴页面 ViewModel。
 * 暴露今日提醒列表（按时间排序）与完成统计，并维护实时「当前时间」用于指示当前进度。
 *
 * 修复：
 * - 跨天刷新：todayDate Flow 每分钟检查日期变更，午夜后自动重新查询今日数据
 * - 定时器对齐到整分钟边界，减少不必要的唤醒
 * - stats 从 items 派生，不额外订阅数据库
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TimelineViewModel(private val repository: PlanRepository) : ViewModel() {

    /** 当前日期字符串，每分钟检查一次，跨天时自动更新 */
    private val _todayDate = MutableStateFlow(DateTimeUtils.today())
    val todayDate: StateFlow<String> = _todayDate.asStateFlow()

    /** 今日提醒列表：依赖 todayDate，跨天时自动重新查询 */
    val items: StateFlow<List<TimelineItem>> =
        _todayDate.flatMapLatest { date ->
            repository.observeTodayTimeline(date)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** 完成统计（按运动/饮食/饮水及整体），从 items 派生避免额外数据库查询 */
    val stats: StateFlow<TodayStats> =
        items.map { computeStats(it) }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodayStats(
                    CompletionStat(PlanType.EXERCISE, 0, 0),
                    CompletionStat(PlanType.DIET, 0, 0),
                    CompletionStat(PlanType.WATER, 0, 0),
                    CompletionStat(PlanType.SKINCARE, 0, 0),
                    CompletionStat(PlanType.MINDFULNESS, 0, 0),
                    CompletionStat(PlanType.OTHER, 0, 0)
                )
            )

    /** 当前时间（总分钟数），每分钟对齐刷新，用于时间轴「现在」指示线 */
    private val _nowMinutes = MutableStateFlow(DateTimeUtils.nowMinutes())
    val nowMinutes: StateFlow<Int> = _nowMinutes.asStateFlow()

    init {
        // 启动定时器，对齐到下一个整分钟边界后每分钟刷新
        viewModelScope.launch {
            while (true) {
                // 计算到下一个整分钟的剩余毫秒数，减少不必要唤醒
                val calendar = Calendar.getInstance()
                val seconds = calendar.get(Calendar.SECOND)
                val millis = calendar.get(Calendar.MILLISECOND)
                val delayMs = ((60 - seconds) * 1000L - millis).coerceAtLeast(1000L)
                delay(delayMs)
                // 更新当前分钟数（驱动「现在」指示线）
                _nowMinutes.value = DateTimeUtils.nowMinutes()
                // 检查是否跨天，若日期变更则触发今日列表重新查询
                val newDate = DateTimeUtils.today()
                if (newDate != _todayDate.value) {
                    _todayDate.value = newDate
                }
            }
        }
    }

    /** 切换某条提醒的完成状态 */
    fun toggleComplete(item: TimelineItem) {
        viewModelScope.launch {
            repository.toggleComplete(item.planId, item.reminderTimeId, _todayDate.value)
        }
    }

    /** 计算各类别完成统计 */
    private fun computeStats(items: List<TimelineItem>): TodayStats {
        fun stat(type: PlanType) =
            CompletionStat(type, items.count { it.type == type }, items.count { it.type == type && it.isCompleted })
        return TodayStats(
            exercise = stat(PlanType.EXERCISE),
            diet = stat(PlanType.DIET),
            water = stat(PlanType.WATER),
            skincare = stat(PlanType.SKINCARE),
            mindfulness = stat(PlanType.MINDFULNESS),
            overall = CompletionStat(PlanType.OTHER, items.size, items.count { it.isCompleted })
        )
    }

    companion object {
        fun factory(container: AppContainer) = VmFactory { TimelineViewModel(container.repository) }
    }
}
