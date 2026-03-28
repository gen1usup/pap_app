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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StickyNote2
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDate
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.core.util.toReadableDuration
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.service.ActiveLaborRecommendation
import com.dadnavigator.app.domain.service.ActiveLaborRecommendationSource
import com.dadnavigator.app.presentation.component.ActionCard
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.TimelineActionButton
import com.dadnavigator.app.presentation.component.TimelineItem
import com.dadnavigator.app.presentation.component.recommendationHeadlineRes
import com.dadnavigator.app.presentation.component.recommendationTextRes
import com.dadnavigator.app.presentation.navigation.AppDestination
import java.time.LocalDate

private data class QuickAction(
    val route: String,
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector
)

@Composable
fun DashboardScreen(
    userId: String,
    widthSizeClass: WindowWidthSizeClass,
    onMenu: (() -> Unit)? = null,
    onOpenTimeline: (() -> Unit)? = null,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    val message = state.errorRes?.let { stringResource(id = it) }
        ?: state.infoRes?.let { stringResource(id = it) }
    val laborStartedTitle = stringResource(id = R.string.events_action_labor_started)
    val laborStartedDescription = stringResource(id = R.string.events_action_labor_started_desc)
    val birthTitle = stringResource(id = R.string.events_action_birth)

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    DashboardContent(
        state = state,
        widthSizeClass = widthSizeClass,
        onMenu = onMenu,
        onOpenTimeline = onOpenTimeline,
        snackbarHostState = snackbarHostState,
        onStartLabor = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.markLaborStarted(
                eventTitle = laborStartedTitle,
                eventDescription = laborStartedDescription
            )
            onNavigate(AppDestination.Events.route)
        },
        onToggleContraction = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.toggleContraction()
        },
        onMarkBirth = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.markBirthNow(eventTitle = birthTitle)
        },
        onNavigate = onNavigate
    )
}

@Composable
private fun DashboardContractionLiveCard(
    state: DashboardUiState,
    onToggleContraction: () -> Unit,
    onOpenDetails: () -> Unit
) {
    InfoCard(
        title = stringResource(id = dashboardRecommendationHeadlineRes(state.recommendation)),
        description = stringResource(id = dashboardRecommendationTextRes(state.recommendation)),
        icon = Icons.Outlined.MonitorHeart,
        overline = stringResource(id = R.string.dashboard_live_contractions_overline)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
            Text(
                text = stringResource(
                    id = if (state.isContractionRunning) {
                        R.string.dashboard_live_contraction_running
                    } else {
                        R.string.dashboard_live_contraction_waiting
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
            ) {
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(id = R.string.dashboard_live_contraction_status_label),
                    value = if (state.isContractionRunning) {
                        state.currentContractionDuration.toReadableDuration()
                    } else {
                        stringResource(id = R.string.status_inactive)
                    }
                )
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(id = R.string.dashboard_live_contraction_interval_label),
                    value = state.currentInterval?.toReadableDuration()
                        ?: stringResource(id = R.string.unknown)
                )
            }
            PrimaryButton(
                text = stringResource(
                    id = if (state.isContractionRunning) {
                        R.string.stop_contraction
                    } else {
                        R.string.start_contraction
                    }
                ),
                onClick = onToggleContraction,
                icon = Icons.Outlined.MonitorHeart
            )
            SecondaryButton(
                text = stringResource(id = R.string.dashboard_open_contraction_cta),
                onClick = onOpenDetails,
                icon = Icons.Outlined.AccessTime
            )
        }
    }
}

@Composable
private fun LaborQuickActionsCard(
    onOpenContacts: () -> Unit
) {
    InfoCard(
        title = stringResource(id = R.string.dashboard_labor_quick_actions_title),
        description = stringResource(id = R.string.dashboard_labor_quick_actions_description),
        icon = Icons.Outlined.Route,
        overline = stringResource(id = R.string.dashboard_labor_quick_actions_overline)
    ) {
        SecondaryButton(
            text = stringResource(id = R.string.dashboard_open_contacts_action),
            onClick = onOpenContacts,
            icon = Icons.Outlined.LocalHospital
        )
    }
}

@Composable
private fun DashboardMilestoneButton(
    text: String,
    onClick: () -> Unit
) {
    PrimaryButton(
        text = text,
        onClick = onClick
    )
}

@Composable
private fun DashboardBabyCard(
    state: DashboardUiState,
    onClick: () -> Unit
) {
    InfoCard(
        title = state.laborSummary.babyName?.takeIf { it.isNotBlank() }
            ?: stringResource(id = R.string.baby_card_title_empty),
        description = state.laborSummary.birthTime?.toReadableDateTime()
            ?: stringResource(id = R.string.baby_birth_unknown),
        icon = Icons.Outlined.ChildCare,
        overline = stringResource(id = R.string.baby_card_overline)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xs)) {
            Text(
                text = stringResource(
                    id = R.string.baby_metrics_value,
                    state.laborSummary.birthWeightGrams?.toString() ?: stringResource(id = R.string.unknown),
                    state.laborSummary.birthHeightCm?.toString() ?: stringResource(id = R.string.unknown)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SecondaryButton(
                text = stringResource(id = R.string.baby_open_action),
                onClick = onClick
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    InfoCard(
        modifier = modifier,
        title = value,
        description = label,
        icon = Icons.Outlined.AccessTime
    )
}

@Composable
private fun DashboardPrimaryCard(
    state: DashboardUiState,
    onNavigate: (String) -> Unit
) {
    val icon = when (state.appStage) {
        AppStage.PREPARING -> Icons.Outlined.Route
        AppStage.LABOR -> Icons.Outlined.MonitorHeart
        AppStage.BABY_BORN -> Icons.Outlined.ChildCare
    }
    InfoCard(
        title = stringResource(id = primaryCardTitleRes(state)),
        description = stringResource(id = primaryCardDescriptionRes(state)),
        icon = icon,
        overline = stringResource(id = R.string.dashboard_focus_overline)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
            when (state.appStage) {
                AppStage.PREPARING -> {
                    val progress = if (state.stageChecklistTotalCount == 0) 0f else {
                        state.stageChecklistCompletedCount.toFloat() / state.stageChecklistTotalCount.toFloat()
                    }

                    Text(
                        text = stringResource(
                            id = R.string.dashboard_checklist_description,
                            state.stageChecklistCompletedCount,
                            state.stageChecklistTotalCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { progress }
                    )
                    SecondaryButton(
                        text = stringResource(id = R.string.dashboard_checklist_action),
                        onClick = { onNavigate(AppDestination.Checklist.route) },
                        icon = Icons.Outlined.Checklist
                    )
                    if (state.showContractionShortcut) {
                        SecondaryButton(
                            text = stringResource(id = R.string.dashboard_open_contraction_cta),
                            onClick = { onNavigate(AppDestination.Contraction.route) },
                            icon = Icons.Outlined.AccessTime
                        )
                    }
                }

                AppStage.LABOR -> {
                    PrimaryButton(
                        text = stringResource(id = R.string.nav_events),
                        onClick = { onNavigate(AppDestination.Events.route) },
                        icon = Icons.Outlined.MonitorHeart
                    )
                    SecondaryButton(
                        text = stringResource(id = R.string.dashboard_open_contraction_cta),
                        onClick = { onNavigate(AppDestination.Contraction.route) },
                        icon = Icons.Outlined.AccessTime
                    )
                }

                AppStage.BABY_BORN -> {
                    PrimaryButton(
                        text = stringResource(id = R.string.baby_open_action),
                        onClick = { onNavigate(AppDestination.Baby.route) },
                        icon = Icons.Outlined.BabyChangingStation
                    )
                    SecondaryButton(
                        text = stringResource(id = R.string.action_checklists),
                        onClick = { onNavigate(AppDestination.Checklist.route) },
                        icon = Icons.Outlined.Checklist
                    )
                    SecondaryButton(
                        text = stringResource(id = R.string.nav_journal),
                        onClick = { onNavigate(AppDestination.Timeline.route) },
                        icon = Icons.Outlined.StickyNote2
                    )
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
    val progress = if (state.stageChecklistTotalCount == 0) 0f else {
        state.stageChecklistCompletedCount.toFloat() / state.stageChecklistTotalCount.toFloat()
    }
    InfoCard(
        modifier = modifier,
        icon = Icons.Outlined.Checklist,
        overline = stringResource(id = R.string.dashboard_checklist_overline),
        title = stringResource(id = dashboardChecklistTitleRes(state.appStage)),
        description = stringResource(
            id = R.string.dashboard_checklist_description,
            state.stageChecklistCompletedCount,
            state.stageChecklistTotalCount
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
private fun StageSupportCard(
    state: DashboardUiState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.showWaterBreakShortcut -> {
            InfoCard(
                modifier = modifier,
                icon = Icons.Outlined.WaterDrop,
                overline = stringResource(id = R.string.dashboard_water_overline),
                title = stringResource(id = R.string.dashboard_water_title),
                description = if (state.hasActiveWaterBreak) {
                    stringResource(
                        id = R.string.dashboard_water_active_description,
                        state.waterBreakElapsed?.toReadableDuration() ?: "--"
                    )
                } else {
                    stringResource(id = R.string.dashboard_water_idle_description)
                }
            ) {
                SecondaryButton(
                    text = stringResource(id = R.string.dashboard_water_action),
                    onClick = { onNavigate(AppDestination.WaterBreak.route) }
                )
            }
        }

        state.appStage == AppStage.BABY_BORN -> {
            InfoCard(
                modifier = modifier,
                icon = Icons.Outlined.BabyChangingStation,
                overline = stringResource(id = R.string.dashboard_after_birth_overline),
                title = stringResource(id = R.string.dashboard_after_birth_title),
                description = stringResource(id = R.string.dashboard_after_birth_description)
            ) {
                SecondaryButton(
                    text = stringResource(id = R.string.baby_open_action),
                    onClick = { onNavigate(AppDestination.Baby.route) }
                )
            }
        }

        else -> {
            InfoCard(
                modifier = modifier,
                icon = Icons.Outlined.LocalHospital,
                overline = stringResource(id = R.string.dashboard_decision_overline),
                title = stringResource(id = R.string.action_when_go_hospital),
                description = stringResource(id = R.string.action_when_go_hospital_desc)
            ) {
                SecondaryButton(
                    text = stringResource(id = R.string.decision_title),
                    onClick = { onNavigate(AppDestination.Decision.route) }
                )
            }
        }
    }
}

private fun quickActionsForStage(stage: AppStage): List<QuickAction> = when (stage) {
    AppStage.PREPARING -> listOf(
        QuickAction(AppDestination.Checklist.route, R.string.action_checklists, R.string.action_checklists_desc, Icons.Outlined.Checklist),
        QuickAction(AppDestination.Decision.route, R.string.action_when_go_hospital, R.string.action_when_go_hospital_desc, Icons.Outlined.LocalHospital),
        QuickAction(AppDestination.Timeline.route, R.string.nav_journal, R.string.action_timeline_desc, Icons.Outlined.StickyNote2),
        QuickAction(AppDestination.Settings.route, R.string.action_settings, R.string.action_settings_desc, Icons.Outlined.Settings)
    )
    AppStage.LABOR -> listOf(
        QuickAction(AppDestination.Events.route, R.string.nav_events, R.string.events_main_tools_description, Icons.Outlined.MonitorHeart),
        QuickAction(AppDestination.WaterBreak.route, R.string.action_water_break_timer, R.string.action_water_break_timer_desc, Icons.Outlined.WaterDrop),
        QuickAction(AppDestination.Decision.route, R.string.action_when_go_hospital, R.string.action_when_go_hospital_desc, Icons.Outlined.LocalHospital),
        QuickAction(AppDestination.Timeline.route, R.string.nav_journal, R.string.action_timeline_desc, Icons.Outlined.StickyNote2)
    )
    AppStage.BABY_BORN -> listOf(
        QuickAction(AppDestination.Baby.route, R.string.baby_open_action, R.string.baby_edit_description, Icons.Outlined.BabyChangingStation),
        QuickAction(AppDestination.Checklist.route, R.string.action_checklists, R.string.action_checklists_desc, Icons.Outlined.Checklist),
        QuickAction(AppDestination.EmergencyContacts.route, R.string.emergency_contacts_title, R.string.emergency_contacts_subtitle, Icons.Outlined.LocalHospital),
        QuickAction(AppDestination.Timeline.route, R.string.nav_journal, R.string.action_timeline_desc, Icons.Outlined.StickyNote2)
    )
}

private fun primaryCardTitleRes(state: DashboardUiState): Int = when (state.appStage) {
    AppStage.PREPARING -> if ((state.daysUntilDueDate ?: Long.MAX_VALUE) <= 14L) {
        R.string.dashboard_preparing_ready_title
    } else {
        R.string.dashboard_preparing_calm_title
    }
    AppStage.LABOR -> R.string.dashboard_labor_title
    AppStage.BABY_BORN -> R.string.dashboard_after_birth_title
}

private fun primaryCardDescriptionRes(state: DashboardUiState): Int = when (state.appStage) {
    AppStage.PREPARING -> if ((state.daysUntilDueDate ?: Long.MAX_VALUE) <= 14L) {
        R.string.dashboard_preparing_ready_description
    } else {
        R.string.dashboard_preparing_calm_description
    }
    AppStage.LABOR -> if (state.hasActiveWaterBreak) {
        R.string.dashboard_labor_water_description
    } else {
        R.string.dashboard_labor_description
    }
    AppStage.BABY_BORN -> R.string.dashboard_after_birth_description
}

private fun dashboardChecklistTitleRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.dashboard_checklist_title_preparing
    AppStage.LABOR -> R.string.dashboard_checklist_title_labor
    AppStage.BABY_BORN -> R.string.dashboard_checklist_title_after_birth
}

private fun dashboardStageTitleRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.LABOR -> R.string.app_stage_labor
    AppStage.BABY_BORN -> R.string.app_stage_baby_born
}

private fun dueDateDescription(dueDate: LocalDate, daysUntilDueDate: Long?): String {
    val base = "ПДР: ${dueDate.toReadableDate()}"
    return when {
        daysUntilDueDate == null -> base
        daysUntilDueDate > 1 -> "$base • осталось $daysUntilDueDate дней"
        daysUntilDueDate == 1L -> "$base • остался 1 день"
        daysUntilDueDate == 0L -> "$base • ориентир на сегодня"
        daysUntilDueDate == -1L -> "$base • ориентир был вчера"
        else -> "$base • прошло ${kotlin.math.abs(daysUntilDueDate)} дней"
    }
}

private fun eventTitle(event: TimelineEvent): String = when (event.type) {
    TimelineType.CONTRACTION -> "Схватка"
    TimelineType.WATER_BREAK -> "Отошли воды"
    TimelineType.LABOR -> event.title.ifBlank { "Событие родов" }
    TimelineType.BIRTH -> event.title.ifBlank { "Ребенок родился" }
    TimelineType.PREPARATION_NOTE,
    TimelineType.LABOR_NOTE,
    TimelineType.BABY_NOTE -> if (event.title.isBlank()) "Заметка" else event.title
    TimelineType.FEEDING -> "Кормление"
    TimelineType.DIAPER -> "Подгузник"
    TimelineType.SLEEP -> "Сон"
    TimelineType.NOTE -> if (event.title.isBlank()) "Заметка" else event.title
}

private fun dashboardRecommendationHeadlineRes(recommendation: ActiveLaborRecommendation): Int = when (recommendation.source) {
    ActiveLaborRecommendationSource.CONTRACTIONS -> recommendationHeadlineRes(recommendation.level)
    ActiveLaborRecommendationSource.WATER_BREAK_CLEAR -> R.string.active_labor_recommendation_water_title
    ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR -> R.string.active_labor_recommendation_urgent_title
    ActiveLaborRecommendationSource.BIRTH_RECORDED -> R.string.active_labor_recommendation_birth_title
}

private fun dashboardRecommendationTextRes(recommendation: ActiveLaborRecommendation): Int = when (recommendation.source) {
    ActiveLaborRecommendationSource.CONTRACTIONS -> recommendationTextRes(recommendation.level)
    ActiveLaborRecommendationSource.WATER_BREAK_CLEAR -> R.string.active_labor_recommendation_water_text
    ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR -> R.string.active_labor_recommendation_urgent_text
    ActiveLaborRecommendationSource.BIRTH_RECORDED -> R.string.active_labor_recommendation_birth_text
}
private fun eventIcon(type: TimelineType): ImageVector = when (type) {
    TimelineType.CONTRACTION -> Icons.Outlined.MonitorHeart
    TimelineType.WATER_BREAK -> Icons.Outlined.WaterDrop
    TimelineType.LABOR -> Icons.Outlined.LocalHospital
    TimelineType.BIRTH -> Icons.Outlined.ChildCare
    TimelineType.PREPARATION_NOTE,
    TimelineType.LABOR_NOTE,
    TimelineType.BABY_NOTE -> Icons.Outlined.StickyNote2
    TimelineType.FEEDING -> Icons.Outlined.FavoriteBorder
    TimelineType.DIAPER -> Icons.Outlined.BabyChangingStation
    TimelineType.SLEEP -> Icons.Outlined.AccessTime
    TimelineType.NOTE -> Icons.Outlined.StickyNote2
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 412)
@Composable
private fun DashboardPreview() {
    DadNavigatorTheme(dynamicColor = false) {
        DashboardContent(
            state = DashboardUiState(
                fatherName = "Алексей",
                appStage = AppStage.LABOR,
                dueDate = LocalDate.now().plusDays(3),
                daysUntilDueDate = 3,
                hasActiveContractionSession = true,
                showLiveContractionBlock = true,
                showLaborQuickActions = true,
                stageChecklistCompletedCount = 4,
                stageChecklistTotalCount = 9,
                showContractionShortcut = true
            ),
            widthSizeClass = WindowWidthSizeClass.Compact,
            onMenu = {},
            onOpenTimeline = {},
            snackbarHostState = remember { SnackbarHostState() },
            onStartLabor = {},
            onToggleContraction = {},
            onMarkBirth = {},
            onNavigate = {}
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    widthSizeClass: WindowWidthSizeClass,
    onMenu: (() -> Unit)?,
    onOpenTimeline: (() -> Unit)?,
    snackbarHostState: SnackbarHostState,
    onStartLabor: () -> Unit,
    onToggleContraction: () -> Unit,
    onMarkBirth: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val spacing = DadTheme.spacing
    val quickActions = quickActionsForStage(state.appStage)
    val showContractionsFirst = state.appStage == AppStage.LABOR && state.showLiveContractionBlock

    ScreenScaffold(
        title = stringResource(id = R.string.dashboard_title),
        subtitle = stringResource(
            id = R.string.dashboard_header_stage,
            stringResource(id = dashboardStageTitleRes(state.appStage))
        ),
        onBack = null,
        onMenu = onMenu,
        snackbarHostState = snackbarHostState,
        actions = {
            if (onOpenTimeline != null) {
                TimelineActionButton(onClick = onOpenTimeline)
            }
        }
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
                if (state.appStage == AppStage.BABY_BORN) {
                    item {
                        DashboardBabyCard(
                            state = state,
                            onClick = { onNavigate(AppDestination.Baby.route) }
                        )
                    }
                }

                if (showContractionsFirst) {
                    item {
                        DashboardContractionLiveCard(
                            state = state,
                            onToggleContraction = onToggleContraction,
                            onOpenDetails = { onNavigate(AppDestination.Contraction.route) }
                        )
                    }
                    if (state.showWaterBreakShortcut) {
                        item {
                            StageSupportCard(
                                state = state,
                                onNavigate = onNavigate
                            )
                        }
                    }
                    item {
                        DashboardMilestoneButton(
                            text = stringResource(id = R.string.events_action_birth),
                            onClick = onMarkBirth
                        )
                    }
                    item {
                        LaborQuickActionsCard(
                            onOpenContacts = { onNavigate(AppDestination.EmergencyContacts.route) }
                        )
                    }
                    item {
                        DashboardChecklistCard(
                            state = state,
                            onClick = { onNavigate(AppDestination.Checklist.route) }
                        )
                    }
                }

                if (state.showDueDateReminder) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.dashboard_due_date_missing_title),
                            description = stringResource(id = R.string.dashboard_due_date_missing_description),
                            icon = Icons.Outlined.Settings,
                            overline = stringResource(id = R.string.dashboard_due_date_overline)
                        ) {
                            SecondaryButton(
                                text = stringResource(id = R.string.dashboard_due_date_missing_action),
                                onClick = { onNavigate(AppDestination.Settings.route) }
                            )
                        }
                    }
                }

                if (state.dueDate != null) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.dashboard_due_date_title),
                            description = dueDateDescription(state.dueDate, state.daysUntilDueDate),
                            icon = Icons.Outlined.AccessTime,
                            overline = stringResource(id = R.string.dashboard_due_date_overline)
                        )
                    }
                }

                if (!showContractionsFirst) {
                    item {
                        if (state.showLiveContractionBlock) {
                            DashboardContractionLiveCard(
                                state = state,
                                onToggleContraction = onToggleContraction,
                                onOpenDetails = { onNavigate(AppDestination.Contraction.route) }
                            )
                        } else {
                            DashboardPrimaryCard(
                                state = state,
                                onNavigate = onNavigate
                            )
                        }
                    }

                    if (state.appStage == AppStage.PREPARING) {
                        item {
                            DashboardMilestoneButton(
                                text = stringResource(id = R.string.dashboard_action_contractions_started),
                                onClick = onStartLabor
                            )
                        }
                    }
                }

                if (state.showBirthDetailsCard) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.dashboard_birth_details_title),
                            description = stringResource(id = R.string.dashboard_birth_details_description),
                            icon = Icons.Outlined.BabyChangingStation
                        ) {
                            SecondaryButton(
                                text = stringResource(id = R.string.baby_open_action),
                                onClick = { onNavigate(AppDestination.Baby.route) }
                            )
                        }
                    }
                }

                if (!showContractionsFirst && state.showLaborQuickActions) {
                    if (state.showWaterBreakShortcut) {
                        item {
                            StageSupportCard(
                                state = state,
                                onNavigate = onNavigate
                            )
                        }
                    }
                    item {
                        DashboardMilestoneButton(
                            text = stringResource(id = R.string.events_action_birth),
                            onClick = onMarkBirth
                        )
                    }
                    item {
                        LaborQuickActionsCard(
                            onOpenContacts = { onNavigate(AppDestination.EmergencyContacts.route) }
                        )
                    }
                    item {
                        DashboardChecklistCard(
                            state = state,
                            onClick = { onNavigate(AppDestination.Checklist.route) }
                        )
                    }
                } else if (!showContractionsFirst) {
                    item {
                        when {
                            state.appStage == AppStage.PREPARING -> {
                                StageSupportCard(
                                    state = state,
                                    onNavigate = onNavigate
                                )
                            }

                            widthSizeClass == WindowWidthSizeClass.Expanded -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                                ) {
                                    DashboardChecklistCard(
                                        modifier = Modifier.weight(1f),
                                        state = state,
                                        onClick = { onNavigate(AppDestination.Checklist.route) }
                                    )
                                    StageSupportCard(
                                        modifier = Modifier.weight(1f),
                                        state = state,
                                        onNavigate = onNavigate
                                    )
                                }
                            }

                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                                    if (state.checklistFirst) {
                                        DashboardChecklistCard(
                                            state = state,
                                            onClick = { onNavigate(AppDestination.Checklist.route) }
                                        )
                                        StageSupportCard(
                                            state = state,
                                            onNavigate = onNavigate
                                        )
                                    } else {
                                        StageSupportCard(
                                            state = state,
                                            onNavigate = onNavigate
                                        )
                                        DashboardChecklistCard(
                                            state = state,
                                            onClick = { onNavigate(AppDestination.Checklist.route) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(id = R.string.quick_actions),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = spacing.xs),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.md),
                            contentPadding = PaddingValues(end = spacing.xs)
                        ) {
                            items(quickActions) { action ->
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
                }

                item {
                    Text(
                        text = stringResource(id = R.string.dashboard_recent_events_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = spacing.sm),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (state.recentEvents.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.dashboard_recent_events_empty_title),
                            description = stringResource(id = R.string.dashboard_recent_events_empty_description),
                            icon = Icons.Outlined.StickyNote2,
                            action = {
                                SecondaryButton(
                                    text = stringResource(id = R.string.nav_journal),
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





