package com.dadnavigator.app.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.AppStage

@Composable
internal fun AppDrawerContent(
    currentRoute: String?,
    currentStage: AppStage,
    birthRecorded: Boolean,
    onStageSelected: (AppStage) -> Unit,
    onServiceSelected: (AppDestination) -> Unit
) {
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
                text = stringResource(id = R.string.drawer_stages_group),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AppStage.entries.forEach { stage ->
            NavigationDrawerItem(
                label = { Text(text = stringResource(id = stageLabelRes(stage))) },
                selected = currentRoute == AppDestination.StageDetails.routeFor(stage),
                onClick = { onStageSelected(stage) },
                icon = {
                    Icon(
                        imageVector = stageIcon(stage),
                        contentDescription = null
                    )
                },
                badge = {
                    if (currentStage == stage) {
                        Badge {
                            Text(text = stringResource(id = R.string.drawer_active_stage_badge))
                        }
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.drawer_services_group),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        drawerServiceDestinations.forEach { item ->
            NavigationDrawerItem(
                label = { Text(text = stringResource(id = item.destination.titleRes)) },
                selected = currentRoute == item.destination.route,
                onClick = { onServiceSelected(item.destination) },
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

internal fun stageLabelRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.CONTRACTIONS -> R.string.app_stage_contractions
    AppStage.AT_HOSPITAL -> R.string.app_stage_at_hospital
    AppStage.AT_HOME -> R.string.app_stage_at_home
}
