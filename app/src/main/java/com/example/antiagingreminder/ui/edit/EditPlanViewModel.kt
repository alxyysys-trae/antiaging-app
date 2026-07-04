package com.example.antiagingreminder.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiagingreminder.data.PlanType
import com.example.antiagingreminder.data.repository.PlanRepository
import com.example.antiagingreminder.di.AppContainer
import com.example.antiagingreminder.domain.PlanDetail
import com.example.antiagingreminder.domain.PlanTime
import com.example.antiagingreminder.ui.VmFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 可编辑时间点（在编辑页临时使用，可能尚未保存）。
 */
data class EditTime(val tempId: Long, val hour: Int, val minute: Int)

/** 编辑页状态 */
data class EditPlanState(
    val id: Long = -1L,
    val title: String = "",
    val description: String = "",
    val type: PlanType = PlanType.EXERCISE,
    val isActive: Boolean = true,
    val repeatDays: Set<Int> = emptySet(), // 空集合表示每天
    val times: List<EditTime> = listOf(EditTime(1, 7, 0)),
    val isLoading: Boolean = true,
    val saved: Boolean = false
) {
    val isValid: Boolean
        get() = title.isNotBlank() && times.isNotEmpty()
}

/**
 * 新建/编辑计划页面 ViewModel。
 * 负责加载已有计划、维护可编辑状态，以及保存与删除。
 */
class EditPlanViewModel(
    private val repository: PlanRepository,
    private val planId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(EditPlanState(id = planId))
    val state: StateFlow<EditPlanState> = _state.asStateFlow()

    init {
        if (planId > 0) loadPlan() else _state.update { it.copy(isLoading = false) }
    }

    private fun loadPlan() {
        viewModelScope.launch {
            repository.getPlanDetail(planId)?.let { detail ->
                _state.value = EditPlanState(
                    id = detail.id,
                    title = detail.title,
                    description = detail.description,
                    type = detail.type,
                    isActive = detail.isActive,
                    repeatDays = detail.repeatDays,
                    times = detail.times.mapIndexed { index, t -> EditTime(t.id.takeIf { it > 0 } ?: index.toLong() + 1, t.hour, t.minute) },
                    isLoading = false
                )
            } ?: _state.update { it.copy(isLoading = false) }
        }
    }

    // ---------------- 字段更新 ----------------
    fun onTitleChange(v: String) = _state.update { it.copy(title = v) }
    fun onDescriptionChange(v: String) = _state.update { it.copy(description = v) }
    fun onTypeChange(v: PlanType) = _state.update { it.copy(type = v) }
    fun onActiveChange(v: Boolean) = _state.update { it.copy(isActive = v) }

    /** 切换某星期是否重复（1=周一…7=周日） */
    fun toggleDay(day: Int) = _state.update {
        val newDays = if (day in it.repeatDays) it.repeatDays - day else it.repeatDays + day
        it.copy(repeatDays = newDays)
    }

    /** 添加一个新的时间点（默认 08:00） */
    fun addTime() = _state.update {
        val nextTempId = (it.times.maxOfOrNull { e -> e.tempId } ?: 0) + 1
        it.copy(times = it.times + EditTime(nextTempId, 8, 0))
    }

    /** 更新某个时间点（自动去重：与已有相同时间点冲突时忽略） */
    fun updateTime(tempId: Long, hour: Int, minute: Int) = _state.update {
        val duplicate = it.times.any { e -> e.tempId != tempId && e.hour == hour && e.minute == minute }
        if (duplicate) return@update it // 忽略重复时间
        it.copy(times = it.times.map { e -> if (e.tempId == tempId) EditTime(tempId, hour, minute) else e })
    }

    /** 删除某个时间点 */
    fun removeTime(tempId: Long) = _state.update {
        it.copy(times = it.times.filterNot { e -> e.tempId == tempId })
    }

    // ---------------- 保存 / 删除 ----------------

    /** 保存（新建或更新） */
    fun save() {
        val s = _state.value
        if (!s.isValid) return
        viewModelScope.launch {
            val detail = PlanDetail(
                id = if (s.id > 0) s.id else 0,
                title = s.title.trim(),
                description = s.description.trim(),
                type = s.type,
                isActive = s.isActive,
                repeatDays = s.repeatDays,
                times = s.times.sortedBy { it.hour * 60 + it.minute }
                    .mapIndexed { idx, e -> PlanTime(idx.toLong(), e.hour, e.minute) }
            )
            if (s.id > 0) repository.updatePlan(detail) else repository.createPlan(detail)
            _state.update { it.copy(saved = true) }
        }
    }

    /** 删除当前计划 */
    fun delete() {
        if (planId <= 0) return
        viewModelScope.launch {
            repository.deletePlan(planId)
            _state.update { it.copy(saved = true) }
        }
    }

    companion object {
        fun factory(container: AppContainer, planId: Long) =
            VmFactory { EditPlanViewModel(container.repository, planId) }
    }
}
