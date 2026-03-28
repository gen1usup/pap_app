package com.dadnavigator.app.presentation.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.presentation.feature.stages.StageDetailsScreen
import com.dadnavigator.app.presentation.screen.about.AboutScreen
import com.dadnavigator.app.presentation.screen.baby.BabyScreen
import com.dadnavigator.app.presentation.screen.checklist.ChecklistScreen
import com.dadnavigator.app.presentation.screen.contraction.ContractionScreen
import com.dadnavigator.app.presentation.screen.dashboard.DashboardScreen
import com.dadnavigator.app.presentation.screen.decision.DecisionScreen
import com.dadnavigator.app.presentation.screen.emergencycontacts.EmergencyContactsScreen
import com.dadnavigator.app.presentation.screen.events.EventsScreen
import com.dadnavigator.app.presentation.screen.help.HelpScreen
import com.dadnavigator.app.presentation.screen.settings.SettingsScreen
import com.dadnavigator.app.presentation.screen.timeline.TimelineScreen
import com.dadnavigator.app.presentation.screen.waterbreak.WaterBreakScreen

@Composable
internal fun AppNavGraph(
    navController: NavHostController,
    userId: String,
    widthSizeClass: WindowWidthSizeClass,
    appStage: AppStage,
    onUpdateAppStage: (AppStage) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateWithinShell: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(AppDestination.Dashboard.route) {
            DashboardScreen(
                userId = userId,
                widthSizeClass = widthSizeClass,
                onMenu = onOpenDrawer,
                onOpenTimeline = { onNavigateWithinShell(AppDestination.Timeline.route) },
                onNavigate = onNavigateWithinShell
            )
        }
        composable(AppDestination.Events.route) {
            EventsScreen(
                userId = userId,
                onOpenContraction = { onNavigateWithinShell(AppDestination.Contraction.route) },
                onOpenWaterBreak = { onNavigateWithinShell(AppDestination.WaterBreak.route) },
                onOpenDecision = { onNavigateWithinShell(AppDestination.Decision.route) },
                onOpenLaborDetails = { onNavigateWithinShell(AppDestination.Baby.route) },
                onMenu = onOpenDrawer,
                onOpenTimeline = { onNavigateWithinShell(AppDestination.Timeline.route) }
            )
        }
        composable(AppDestination.Checklist.route) {
            ChecklistScreen(
                userId = userId,
                onBack = null,
                onMenu = onOpenDrawer,
                onOpenTimeline = { onNavigateWithinShell(AppDestination.Timeline.route) }
            )
        }
        composable(AppDestination.EmergencyContacts.route) {
            EmergencyContactsScreen(
                onBack = null,
                onMenu = onOpenDrawer
            )
        }
        composable(AppDestination.Timeline.route) {
            TimelineScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onMenu = null
            )
        }
        composable(AppDestination.Contraction.route) {
            ContractionScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
                onOpenWaterBreak = { onNavigateWithinShell(AppDestination.WaterBreak.route) }
            )
        }
        composable(AppDestination.WaterBreak.route) {
            WaterBreakScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Baby.route) {
            BabyScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Decision.route) {
            DecisionScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Help.route) {
            HelpScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestination.StageDetails.route,
            arguments = listOf(
                navArgument(AppDestination.StageDetails.stageArgument) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val stageValue = backStackEntry.arguments?.getString(AppDestination.StageDetails.stageArgument)
            val stage = AppStage.fromStorage(stageValue)
            StageDetailsScreen(
                stage = stage,
                currentStage = appStage,
                onBack = { navController.popBackStack() },
                onActivate = {
                    onUpdateAppStage(it)
                    onNavigateWithinShell(AppDestination.Dashboard.route)
                }
            )
        }
    }
}
