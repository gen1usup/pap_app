package com.dadnavigator.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dadnavigator.app.domain.model.AppStage
import kotlinx.coroutines.launch

/**
 * Root navigation shell with a focused bottom bar and service drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val currentRoute = currentDestination?.route
    val isTopLevel = currentRoute in topLevelRoutes

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                currentStage = appStage,
                onStageSelected = { stage ->
                    navigateWithinShell(AppDestination.StageDetails.routeFor(stage))
                    scope.launch { drawerState.close() }
                },
                onServiceSelected = { destination ->
                    navigateWithinShell(destination.route)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (isTopLevel) {
                    NavigationBar {
                        bottomNavigationDestinations.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.destination.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateWithinShell(item.destination.route) },
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
            AppNavGraph(
                navController = navController,
                userId = userId,
                widthSizeClass = widthSizeClass,
                appStage = appStage,
                onUpdateAppStage = onUpdateAppStage,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onNavigateWithinShell = ::navigateWithinShell,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
