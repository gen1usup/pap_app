package com.dadnavigator.app.presentation.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dadnavigator.app.presentation.screen.checklist.ChecklistScreen
import com.dadnavigator.app.presentation.screen.contraction.ContractionScreen
import com.dadnavigator.app.presentation.screen.dashboard.DashboardScreen
import com.dadnavigator.app.presentation.screen.decision.DecisionScreen
import com.dadnavigator.app.presentation.screen.labor.LaborScreen
import com.dadnavigator.app.presentation.screen.momsupport.MomSupportScreen
import com.dadnavigator.app.presentation.screen.postpartum.PostpartumScreen
import com.dadnavigator.app.presentation.screen.settings.SettingsScreen
import com.dadnavigator.app.presentation.screen.sos.SosScreen
import com.dadnavigator.app.presentation.screen.timeline.TimelineScreen
import com.dadnavigator.app.presentation.screen.trackers.TrackersScreen
import com.dadnavigator.app.presentation.screen.waterbreak.WaterBreakScreen

/**
 * Main navigation graph.
 */
@Composable
fun AppNavHost(
    userId: String,
    fatherName: String,
    widthSizeClass: WindowWidthSizeClass
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Dashboard.route
    ) {
        composable(AppDestination.Dashboard.route) {
            DashboardScreen(
                userId = userId,
                fatherName = fatherName,
                widthSizeClass = widthSizeClass,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(AppDestination.Contraction.route) {
            ContractionScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.WaterBreak.route) {
            WaterBreakScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Decision.route) {
            DecisionScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Checklist.route) {
            ChecklistScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Sos.route) {
            SosScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.MomSupport.route) {
            MomSupportScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Labor.route) {
            LaborScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Postpartum.route) {
            PostpartumScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Trackers.route) {
            TrackersScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Timeline.route) {
            TimelineScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
