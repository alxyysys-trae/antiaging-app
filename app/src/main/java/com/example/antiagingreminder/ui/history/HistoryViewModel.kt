package com.example.antiagingreminder.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiagingreminder.data.repository.PlanRepository
import com.example.antiagingreminder.di.AppContainer
import com.example.antiagingreminder.domain.TimelineItem
import com.example.antiagingreminder.ui.VmFactory
import com.example.antiagingreminder.util.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/** 历史页面某条记录的展示模型 */
data class HistoryItem(
    val timeline: TimelineItem
)

/**
 * 历史记录页面 ViewModel。
 * 按日期查看过往提醒完成情况，支持日期切换与统计。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val repository: PlanRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateTimeUtils.today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    /** 当前选中日期的全部条目（结合计划配置与历史完成状态） */
    val items: StateFlow<List<TimelineItem>> =
        _selectedDate.flatMapLatest { date ->
            combine(
                repository.observePlansWithTimes(),
                repository.observeHistoryByDate(date)
            ) { plans, records ->
                val iso = DateTimeUtils.isoDayOfWeekForDate(date)
                val doneKeys = records.filter { it.isCompleted }.map { it.reminderTimeId }.toSet()
                plans.filter { it.isActive }
                    .filter { it.repeatDays.isEmpty() || iso in it.repeatDays }
                    .flatMap { plan ->
                        plan.times.map { time ->
                            TimelineItem(
                                planId = plan.id,
                                reminderTimeId = time.id,
                                title = plan.title,
                                description = plan.description,
                                type = plan.type,
                                hour = time.hour,
                                minute = time.minute,
                                isCompleted = time.id in doneKeys
                            )
                        }
                    }
                    .sortedBy { it.hour * 60 + it.minute }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: String) { _selectedDate.value = date }

    /** 获取选中日期的完成统计 */
    fun statsFor(items: List<TimelineItem>): Triple<Int, Int, Float> {
        val total = items.size
        val done = items.count { it.isCompleted }
        val progress = if (total == 0) 0f else done.toFloat() / total
        return Triple(total, done, progress)
    }

    companion object {
        fun factory(container: AppContainer) = VmFactory { HistoryViewModel(container.repository) }
    }
}
