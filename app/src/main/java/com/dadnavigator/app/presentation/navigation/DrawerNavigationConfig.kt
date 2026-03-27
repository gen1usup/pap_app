package com.dadnavigator.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.dadnavigator.app.domain.model.AppStage

internal data class DrawerServiceDestinationUi(
    val destination: AppDestination,
    val icon: ImageVector
)

internal fun stageIcon(stage: AppStage): ImageVector = when (stage) {
    AppStage.PREPARING -> Icons.Outlined.Checklist
    AppStage.CONTRACTIONS -> Icons.Outlined.MonitorHeart
    AppStage.AT_HOSPITAL -> Icons.Outlined.LocalHospital
    AppStage.AT_HOME -> Icons.Outlined.ChildCare
}

internal val drawerServiceDestinations = listOf(
    DrawerServiceDestinationUi(AppDestination.EmergencyContacts, Icons.Outlined.Phone),
    DrawerServiceDestinationUi(AppDestination.Help, Icons.AutoMirrored.Outlined.MenuBook),
    DrawerServiceDestinationUi(AppDestination.About, Icons.Outlined.Info),
    DrawerServiceDestinationUi(AppDestination.Settings, Icons.Outlined.Settings)
)
