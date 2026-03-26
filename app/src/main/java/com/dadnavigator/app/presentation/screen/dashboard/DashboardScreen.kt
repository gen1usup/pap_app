package com.dadnavigator.app.presentation.screen.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.presentation.component.ActionCard
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.navigation.AppDestination

private data class QuickAction(
    val route: String,
    val titleRes: Int,
    val descriptionRes: Int
)

/**
 * Home screen with immediate action shortcuts.
 */
@Composable
fun DashboardScreen(
    userId: String,
    fatherName: String,
    widthSizeClass: WindowWidthSizeClass,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    val actions = listOf(
        QuickAction(AppDestination.Contraction.route, R.string.action_contraction_counter, R.string.action_contraction_counter_desc),
        QuickAction(AppDestination.WaterBreak.route, R.string.action_water_break_timer, R.string.action_water_break_timer_desc),
        QuickAction(AppDestination.Decision.route, R.string.action_when_go_hospital, R.string.action_when_go_hospital_desc),
        QuickAction(AppDestination.Checklist.route, R.string.action_checklists, R.string.action_checklists_desc),
        QuickAction(AppDestination.Sos.route, R.string.action_sos, R.string.action_sos_desc),
        QuickAction(AppDestination.MomSupport.route, R.string.action_help_mom, R.string.action_help_mom_desc),
        QuickAction(AppDestination.Labor.route, R.string.action_labor, R.string.action_labor_desc),
        QuickAction(AppDestination.Postpartum.route, R.string.action_postpartum, R.string.action_postpartum_desc),
        QuickAction(AppDestination.Trackers.route, R.string.action_trackers, R.string.action_trackers_desc),
        QuickAction(AppDestination.Timeline.route, R.string.action_timeline, R.string.action_timeline_desc),
        QuickAction(AppDestination.Settings.route, R.string.action_settings, R.string.action_settings_desc)
    )

    val currentActionHint = if (state.hasActiveContractionSession) {
        stringResource(id = R.string.now_action_open_contraction_session)
    } else {
        null
    }
    val onCurrentActionClick = if (state.hasActiveContractionSession) {
        { onNavigate(AppDestination.Contraction.route) }
    } else {
        null
    }

    ScreenScaffold(
        title = stringResource(id = R.string.dashboard_title),
        onBack = null
    ) { innerPadding ->
        val contentPadding = PaddingValues(
            start = 16.dp,
            top = innerPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = 16.dp
        )

        if (widthSizeClass == WindowWidthSizeClass.Expanded) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(320.dp),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CurrentActionCard(
                        fatherName = fatherName,
                        actionText = stringResource(id = state.currentActionRes),
                        actionHint = currentActionHint,
                        onClick = onCurrentActionClick
                    )
                }
                items(actions) { action ->
                    ActionCard(
                        title = stringResource(id = action.titleRes),
                        description = stringResource(id = action.descriptionRes),
                        onClick = { onNavigate(action.route) }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CurrentActionCard(
                        fatherName = fatherName,
                        actionText = stringResource(id = state.currentActionRes),
                        actionHint = currentActionHint,
                        onClick = onCurrentActionClick
                    )
                }
                items(actions) { action ->
                    ActionCard(
                        title = stringResource(id = action.titleRes),
                        description = stringResource(id = action.descriptionRes),
                        onClick = { onNavigate(action.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentActionCard(
    fatherName: String,
    actionText: String,
    actionHint: String?,
    onClick: (() -> Unit)?
) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .let { baseModifier ->
            if (onClick != null) {
                baseModifier.clickable(onClick = onClick)
            } else {
                baseModifier
            }
        }

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (fatherName.isNotBlank()) {
                Text(
                    text = fatherName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = actionText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (actionHint != null) {
                Text(
                    text = actionHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
