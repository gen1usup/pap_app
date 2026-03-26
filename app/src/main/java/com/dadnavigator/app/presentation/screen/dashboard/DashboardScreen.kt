package com.dadnavigator.app.presentation.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.core.util.toReadableDuration
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.presentation.component.ActionCard
import com.dadnavigator.app.presentation.component.DangerButton
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone
import com.dadnavigator.app.presentation.component.TimelineItem
import com.dadnavigator.app.presentation.navigation.AppDestination
import java.time.Duration
import java.time.Instant

private data class QuickAction(
    val route: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector
)

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
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val errorMessage = state.errorRes?.let { stringResource(id = it) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.dismissError()
        }
    }

    DashboardContent(
        fatherName = fatherName,
        state = state,
        widthSizeClass = widthSizeClass,
        snackbarHostState = snackbarHostState,
        onStartContraction = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            if (!state.hasActiveContractionSession) {
                viewModel.startFirstContraction()
            }
            onNavigate(AppDestination.Contraction.route)
        },
        onOpenSos = { onNavigate(AppDestination.Sos.route) },
        onNavigate = onNavigate
    )
}

@Composable
private fun DashboardContent(
    fatherName: String,
    state: DashboardUiState,
    widthSizeClass: WindowWidthSizeClass,
    snackbarHostState: SnackbarHostState,
    onStartContraction: () -> Unit,
    onOpenSos: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val spacing = DadTheme.spacing
    val actions = listOf(
        QuickAction(AppDestination.WaterBreak.route, R.string.action_water_break_timer, R.string.action_water_break_timer_desc, Icons.Outlined.WaterDrop),
        QuickAction(AppDestination.Decision.route, R.string.action_when_go_hospital, R.string.action_when_go_hospital_desc, Icons.Outlined.LocalHospital),
        QuickAction(AppDestination.Checklist.route, R.string.action_checklists, R.string.action_checklists_desc, Icons.Outlined.Checklist),
        QuickAction(AppDestination.Timeline.route, R.string.action_timeline, R.string.action_timeline_desc, Icons.Outlined.Timeline),
        QuickAction(AppDestination.Trackers.route, R.string.action_trackers, R.string.action_trackers_desc, Icons.Outlined.ChildCare),
        QuickAction(AppDestination.MomSupport.route, R.string.action_help_mom, R.string.action_help_mom_desc, Icons.Outlined.FavoriteBorder),
        QuickAction(AppDestination.Labor.route, R.string.action_labor, R.string.action_labor_desc, Icons.Outlined.MonitorHeart),
        QuickAction(AppDestination.Postpartum.route, R.string.action_postpartum, R.string.action_postpartum_desc, Icons.Outlined.BabyChangingStation),
        QuickAction(AppDestination.Settings.route, R.string.action_settings, R.string.action_settings_desc, Icons.Outlined.Settings)
    )

    ScreenScaffold(
        title = stringResource(id = R.string.dashboard_title),
        subtitle = stringResource(id = R.string.dashboard_subtitle),
        onBack = null,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = spacing.md,
                    top = spacing.xs,
                    end = spacing.md,
                    bottom = spacing.section
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    StatusCard(
                        title = if (fatherName.isBlank()) {
                            stringResource(id = R.string.dashboard_focus_title)
                        } else {
                            stringResource(id = R.string.dashboard_focus_title_named, fatherName)
                        },
                        description = stringResource(id = state.currentActionRes),
                        tone = when {
                            state.hasActiveWaterBreak -> StatusTone.Warning
                            state.hasActiveContractionSession -> StatusTone.Success
                            else -> StatusTone.Calm
                        },
                        icon = if (state.hasActiveWaterBreak) Icons.Outlined.WaterDrop else Icons.Outlined.MonitorHeart,
                        headline = stringResource(id = R.string.dashboard_focus_overline)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            PrimaryButton(
                                text = stringResource(
                                    id = if (state.hasActiveContractionSession) {
                                        R.string.dashboard_open_contraction_cta
                                    } else {
                                        R.string.dashboard_start_contraction_cta
                                    }
                                ),
                                onClick = onStartContraction,
                                icon = Icons.Outlined.AccessTime
                            )
                            DangerButton(
                                text = stringResource(id = R.string.dashboard_sos_cta),
                                onClick = onOpenSos,
                                icon = Icons.Outlined.Emergency
                            )
                        }
                    }
                }

                item {
                    if (widthSizeClass == WindowWidthSizeClass.Expanded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            DashboardChecklistCard(
                                modifier = Modifier.weight(1f),
                                state = state,
                                onClick = { onNavigate(AppDestination.Checklist.route) }
                            )
                            DashboardWaterCard(
                                modifier = Modifier.weight(1f),
                                state = state,
                                onClick = { onNavigate(AppDestination.WaterBreak.route) }
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                            DashboardChecklistCard(
                                state = state,
                                onClick = { onNavigate(AppDestination.Checklist.route) }
                            )
                            DashboardWaterCard(
                                state = state,
                                onClick = { onNavigate(AppDestination.WaterBreak.route) }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(id = R.string.quick_actions),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.xs)
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        contentPadding = PaddingValues(end = spacing.xs)
                    ) {
                        items(actions) { action ->
                            Box(modifier = Modifier.fillParentMaxWidth(0.84f)) {
                                ActionCard(
                                    title = stringResource(id = action.titleRes),
                                    description = stringResource(id = action.descriptionRes),
                                    icon = action.icon,
                                    onClick = { onNavigate(action.route) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(id = R.string.dashboard_recent_events_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.sm)
                    )
                }

                if (state.recentEvents.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.dashboard_recent_events_empty_title),
                            description = stringResource(id = R.string.dashboard_recent_events_empty_description),
                            icon = Icons.Outlined.NightlightRound,
                            action = {
                                SecondaryButton(
                                    text = stringResource(id = R.string.action_timeline),
                                    onClick = { onNavigate(AppDestination.Timeline.route) },
                                    fullWidth = false
                                )
                            }
                        )
                    }
                } else {
                    items(state.recentEvents) { event ->
                        TimelineItem(
                            title = eventTitle(event),
                            subtitle = event.timestamp.toReadableDateTime(),
                            time = event.timestamp.toReadableDateTime().takeLast(5),
                            description = event.description.takeIf { it.isNotBlank() },
                            icon = eventIcon(event.type)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardChecklistCard(
    state: DashboardUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (state.checklistTotalCount == 0) 0f else {
        state.checklistCompletedCount.toFloat() / state.checklistTotalCount.toFloat()
    }
    InfoCard(
        modifier = modifier,
        icon = Icons.Outlined.Checklist,
        overline = stringResource(id = R.string.dashboard_checklist_overline),
        title = stringResource(id = R.string.dashboard_checklist_title),
        description = stringResource(
            id = R.string.dashboard_checklist_description,
            state.checklistCompletedCount,
            state.checklistTotalCount
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress }
            )
            SecondaryButton(
                text = stringResource(id = R.string.dashboard_checklist_action),
                onClick = onClick
            )
        }
    }
}

@Composable
private fun DashboardWaterCard(
    state: DashboardUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatusCard(
        modifier = modifier,
        title = stringResource(id = R.string.dashboard_water_title),
        description = if (state.hasActiveWaterBreak) {
            stringResource(
                id = R.string.dashboard_water_active_description,
                state.waterBreakElapsed?.toReadableDuration() ?: "--"
            )
        } else {
            stringResource(id = R.string.dashboard_water_idle_description)
        },
        tone = if (state.hasActiveWaterBreak) StatusTone.Warning else StatusTone.Calm,
        icon = Icons.Outlined.WaterDrop,
        headline = stringResource(id = R.string.dashboard_water_overline)
    ) {
        SecondaryButton(
            text = stringResource(id = R.string.dashboard_water_action),
            onClick = onClick
        )
    }
}

private fun eventTitle(event: TimelineEvent): String = when (event.type) {
    TimelineType.CONTRACTION -> "Схватка"
    TimelineType.WATER_BREAK -> "Отхождение вод"
    TimelineType.LABOR -> event.title.ifBlank { "Событие родов" }
    TimelineType.BIRTH -> "Рождение ребенка"
    TimelineType.FEEDING -> "Кормление"
    TimelineType.DIAPER -> "Подгузник"
    TimelineType.SLEEP -> "Сон"
    TimelineType.NOTE -> if (event.title.isBlank()) "Заметка" else event.title
}

private fun eventIcon(type: TimelineType): ImageVector = when (type) {
    TimelineType.CONTRACTION -> Icons.Outlined.MoreTime
    TimelineType.WATER_BREAK -> Icons.Outlined.WaterDrop
    TimelineType.LABOR -> Icons.Outlined.MonitorHeart
    TimelineType.BIRTH -> Icons.Outlined.ChildCare
    TimelineType.FEEDING -> Icons.Outlined.FavoriteBorder
    TimelineType.DIAPER -> Icons.Outlined.BabyChangingStation
    TimelineType.SLEEP -> Icons.Outlined.NightlightRound
    TimelineType.NOTE -> Icons.Outlined.Timeline
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun DashboardPreview() {
    DadNavigatorTheme(dynamicColor = false) {
        DashboardContent(
            fatherName = "Алексей",
            state = DashboardUiState(
                hasActiveContractionSession = true,
                checklistCompletedCount = 7,
                checklistTotalCount = 12,
                recentEvents = listOf(
                    TimelineEvent(
                        id = 1,
                        userId = "u",
                        type = TimelineType.CONTRACTION,
                        timestamp = Instant.now(),
                        title = "",
                        description = ""
                    ),
                    TimelineEvent(
                        id = 2,
                        userId = "u",
                        type = TimelineType.WATER_BREAK,
                        timestamp = Instant.now(),
                        title = "",
                        description = "Прозрачные"
                    )
                ),
                currentActionRes = R.string.now_action_contraction,
                waterBreakElapsed = Duration.ofMinutes(42)
            ),
            widthSizeClass = WindowWidthSizeClass.Compact,
            snackbarHostState = remember { SnackbarHostState() },
            onStartContraction = {},
            onOpenSos = {},
            onNavigate = {}
        )
    }
}
