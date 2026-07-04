package com.example.antiagingreminder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.antiagingreminder.di.AppContainer
import com.example.antiagingreminder.notification.NotificationHelper
import com.example.antiagingreminder.ui.edit.EditPlanScreen
import com.example.antiagingreminder.ui.edit.EditPlanViewModel
import com.example.antiagingreminder.ui.history.HistoryScreen
import com.example.antiagingreminder.ui.history.HistoryViewModel
import com.example.antiagingreminder.ui.timeline.TimelineScreen
import com.example.antiagingreminder.ui.timeline.TimelineViewModel
import com.example.antiagingreminder.ui.templates.TemplateScreen
import com.example.antiagingreminder.ui.templates.TemplateViewModel

/** 路由常量 */
object Routes {
    const val TIMELINE = "timeline"
    const val TEMPLATES = "templates"
    const val HISTORY = "history"
    const val EDIT_PLAN = "edit_plan/{planId}"   // planId=-1 表示新建
    const val EDIT_PLAN_NEW = "edit_plan/-1"

    fun editPlan(planId: Long) = "edit_plan/$planId"
}

/**
 * 应用导航图。
 * 集中管理各页面路由与 ViewModel 构建，保证页面间跳转清晰。
 */
@Composable
fun AppNavigation(container: AppContainer, pendingAction: String? = null) {
    val navController: NavHostController = rememberNavController()

    // 通知点击后跳转到今日时间轴
    LaunchedEffect(pendingAction) {
        if (pendingAction == NotificationHelper.ACTION_OPEN_TODAY) {
            navController.navigate(Routes.TIMELINE) {
                popUpTo(Routes.TIMELINE) { inclusive = true }
                launchSingleTop = true
            }
            // 消费后清除，防止 Activity 重建时重复触发
            (navController.context as? android.app.Activity)?.let { activity ->
                activity.intent.action = null
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.TIMELINE) {

        // 今日时间轴
        composable(Routes.TIMELINE) {
            val vm: TimelineViewModel = viewModel(factory = TimelineViewModel.factory(container))
            TimelineScreen(
                viewModel = vm,
                onOpenTemplates = { navController.navigate(Routes.TEMPLATES) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onAddPlan = { navController.navigate(Routes.EDIT_PLAN_NEW) },
                onEditPlan = { navController.navigate(Routes.editPlan(it)) }
            )
        }

        // 模板库
        composable(Routes.TEMPLATES) {
            val vm: TemplateViewModel = viewModel(factory = TemplateViewModel.factory(container))
            TemplateScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onAddNewPlan = { navController.navigate(Routes.EDIT_PLAN_NEW) }
            )
        }

        // 历史记录
        composable(Routes.HISTORY) {
            val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
            HistoryScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        // 新建/编辑计划
        composable(
            route = Routes.EDIT_PLAN,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getLong("planId") ?: -1L
            val vm: EditPlanViewModel = viewModel(factory = EditPlanViewModel.factory(container, planId))
            EditPlanScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
