package com.dadnavigator.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Badge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.presentation.screen.about.AboutScreen
import com.dadnavigator.app.presentation.screen.checklist.ChecklistScreen
import com.dadnavigator.app.presentation.screen.contraction.ContractionScreen
import com.dadnavigator.app.presentation.screen.dashboard.DashboardScreen
import com.dadnavigator.app.presentation.screen.decision.DecisionScreen
import com.dadnavigator.app.presentation.screen.emergencycontacts.EmergencyContactsScreen
import com.dadnavigator.app.presentation.screen.events.EventsScreen
import com.dadnavigator.app.presentation.screen.help.HelpScreen
import com.dadnavigator.app.presentation.screen.labor.LaborScreen
import com.dadnavigator.app.presentation.screen.momsupport.MomSupportScreen
import com.dadnavigator.app.presentation.screen.postpartum.PostpartumScreen
import com.dadnavigator.app.presentation.screen.settings.SettingsScreen
import com.dadnavigator.app.presentation.screen.sos.SosScreen
import com.dadnavigator.app.presentation.screen.timeline.TimelineScreen
import com.dadnavigator.app.presentation.screen.trackers.TrackersScreen
import com.dadnavigator.app.presentation.screen.waterbreak.WaterBreakScreen
import kotlinx.coroutines.launch

private data class TopLevelDestinationUi(
    val destination: AppDestination,
    val icon: ImageVector,
    val labelRes: Int
)

private data class DrawerDestinationUi(
    val destination: AppDestination,
    val icon: ImageVector
)

/**
 * Root navigation host with bottom navigation for primary workflows and drawer for service sections.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppNavHost(
    userId: String,
    widthSizeClass: WindowWidthSizeClass,
    appStage: AppStage,
    onUpdateAppStage: (AppStage) -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val topLevelDestinations = remember {
        listOf(
            TopLevelDestinationUi(AppDestination.Dashboard, Icons.Outlined.Home, R.string.nav_home),
            TopLevelDestinationUi(AppDestination.Events, Icons.Outlined.Widgets, R.string.nav_events),
            TopLevelDestinationUi(AppDestination.Checklist, Icons.Outlined.Checklist, R.string.nav_checklists),
            TopLevelDestinationUi(AppDestination.Timeline, Icons.AutoMirrored.Outlined.StickyNote2, R.string.nav_journal)
        )
    }
    val topLevelRoutes = topLevelDestinations.map { it.destination.route }.toSet()
    fun navigateWithinShell(route: String) {
        if (route in topLevelRoutes) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
    }
    val drawerDestinations = remember {
        listOf(
            DrawerDestinationUi(AppDestination.Sos, Icons.Outlined.Emergency),
            DrawerDestinationUi(AppDestination.EmergencyContacts, Icons.Outlined.Phone),
            DrawerDestinationUi(AppDestination.Decision, Icons.Outlined.LocalHospital),
            DrawerDestinationUi(AppDestination.MomSupport, Icons.Outlined.FavoriteBorder),
            DrawerDestinationUi(AppDestination.Trackers, Icons.Outlined.ChildCare),
            DrawerDestinationUi(AppDestination.Labor, Icons.Outlined.MonitorHeart),
            DrawerDestinationUi(AppDestination.Postpartum, Icons.Outlined.ChildCare),
            DrawerDestinationUi(AppDestination.Help, Icons.AutoMirrored.Outlined.MenuBook),
            DrawerDestinationUi(AppDestination.About, Icons.Outlined.Info),
            DrawerDestinationUi(AppDestination.Settings, Icons.Outlined.Settings)
        )
    }
    val isTopLevel = currentDestination?.route in topLevelRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.safeDrawingPadding()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(id = R.string.drawer_stage_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppStage.entries.forEach { stage ->
                            FilterChip(
                                selected = appStage == stage,
                                onClick = {
                                    onUpdateAppStage(stage)
                                    scope.launch { drawerState.close() }
                                },
                                label = { Text(text = stringResource(id = stageLabelRes(stage))) }
                            )
                        }
                    }
                }
                HorizontalDivider()
                drawerDestinations.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(id = item.destination.titleRes)) },
                        selected = currentDestination?.route == item.destination.route,
                        onClick = {
                            navigateWithinShell(item.destination.route)
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (isTopLevel) {
                    NavigationBar {
                        topLevelDestinations.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.destination.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navigateWithinShell(item.destination.route)
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = stringResource(id = item.labelRes)
                                    )
                                },
                                label = { Text(text = stringResource(id = item.labelRes)) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppDestination.Dashboard.route) {
                    DashboardScreen(
                        userId = userId,
                        widthSizeClass = widthSizeClass,
                        onMenu = { scope.launch { drawerState.open() } },
                        onNavigate = ::navigateWithinShell
                    )
                }
                composable(AppDestination.Events.route) {
                    EventsScreen(
                        userId = userId,
                        onOpenContraction = { navigateWithinShell(AppDestination.Contraction.route) },
                        onOpenWaterBreak = { navigateWithinShell(AppDestination.WaterBreak.route) },
                        onOpenDecision = { navigateWithinShell(AppDestination.Decision.route) },
                        onOpenLaborDetails = { navigateWithinShell(AppDestination.Labor.route) },
                        onMenu = { scope.launch { drawerState.open() } }
                    )
                }
                composable(AppDestination.Checklist.route) {
                    ChecklistScreen(
                        userId = userId,
                        onBack = null,
                        onMenu = { scope.launch { drawerState.open() } }
                    )
                }
                composable(AppDestination.Timeline.route) {
                    TimelineScreen(
                        userId = userId,
                        onBack = null,
                        onMenu = { scope.launch { drawerState.open() } }
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
                composable(AppDestination.Sos.route) {
                    SosScreen(
                        onBack = { navController.popBackStack() },
                        onOpenContacts = { navController.navigate(AppDestination.EmergencyContacts.route) }
                    )
                }
                composable(AppDestination.EmergencyContacts.route) {
                    EmergencyContactsScreen(onBack = { navController.popBackStack() })
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
                composable(AppDestination.Help.route) {
                    HelpScreen(onBack = { navController.popBackStack() })
                }
                composable(AppDestination.About.route) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
                composable(AppDestination.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun stageLabelRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.LABOR -> R.string.app_stage_labor
    AppStage.AFTER_BIRTH -> R.string.app_stage_after_birth
}
