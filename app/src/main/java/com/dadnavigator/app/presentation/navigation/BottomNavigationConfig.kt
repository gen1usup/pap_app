package com.dadnavigator.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import com.dadnavigator.app.R

internal data class TopLevelDestinationUi(
    val destination: AppDestination,
    val icon: ImageVector,
    val labelRes: Int
)

internal val bottomNavigationDestinations = listOf(
    TopLevelDestinationUi(AppDestination.Dashboard, Icons.Outlined.Home, R.string.nav_home),
    TopLevelDestinationUi(AppDestination.Events, Icons.Outlined.Widgets, R.string.nav_events),
    TopLevelDestinationUi(AppDestination.Checklist, Icons.Outlined.Checklist, R.string.nav_checklists)
)

internal val topLevelRoutes = bottomNavigationDestinations.map { it.destination.route }.toSet()
