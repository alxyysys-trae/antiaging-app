package com.example.antiagingreminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 通用 ViewModel 工厂：通过 lambda 构造 ViewModel 实例，
 * 用于在 Compose 中传入依赖（如 AppContainer）创建各页面 ViewModel。
 */
class VmFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}
