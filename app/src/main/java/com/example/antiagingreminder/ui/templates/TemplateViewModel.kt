package com.example.antiagingreminder.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.antiagingreminder.data.local.entity.TemplateEntity
import com.example.antiagingreminder.data.repository.PlanRepository
import com.example.antiagingreminder.di.AppContainer
import com.example.antiagingreminder.ui.VmFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 模板库页面 ViewModel。
 * 暴露模板列表，并支持「一键添加到计划」操作。
 */
class TemplateViewModel(private val repository: PlanRepository) : ViewModel() {

    val templates: StateFlow<List<TemplateEntity>> =
        repository.observeTemplates()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /** 从模板一键添加为计划，添加后可自由修改 */
    fun addFromTemplate(template: TemplateEntity, onAdded: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.addPlanFromTemplate(template)
            onAdded(id)
        }
    }

    companion object {
        fun factory(container: AppContainer) = VmFactory { TemplateViewModel(container.repository) }
    }
}
