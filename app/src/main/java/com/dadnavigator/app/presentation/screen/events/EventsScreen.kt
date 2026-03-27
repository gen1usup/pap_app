package com.dadnavigator.app.presentation.screen.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone

@Composable
fun EventsScreen(
    userId: String,
    onOpenContraction: () -> Unit,
    onOpenWaterBreak: () -> Unit,
    onOpenDecision: () -> Unit,
    onOpenLaborDetails: () -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    viewModel: EventsViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.errorRes?.let { stringResource(id = it) }
        ?: state.infoRes?.let { stringResource(id = it) }
    val laborStartedTitle = stringResource(id = R.string.events_action_labor_started)
    val laborStartedDescription = stringResource(id = R.string.events_action_labor_started_desc)
    val departedTitle = stringResource(id = R.string.events_action_departed)
    val departedDescription = stringResource(id = R.string.events_action_departed_desc)
    val arrivedTitle = stringResource(id = R.string.events_action_arrived)
    val arrivedDescription = stringResource(id = R.string.events_action_arrived_desc)
    val birthTitle = stringResource(id = R.string.events_action_birth)

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    EventsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMenu = onMenu,
        onOpenContraction = onOpenContraction,
        onOpenWaterBreak = onOpenWaterBreak,
        onOpenDecision = onOpenDecision,
        onOpenLaborDetails = onOpenLaborDetails,
        onMarkLaborStarted = {
            viewModel.markLaborStarted(
                eventTitle = laborStartedTitle,
                eventDescription = laborStartedDescription
            )
        },
        onMarkDeparture = {
            viewModel.addJourneyEvent(
                title = departedTitle,
                description = departedDescription
            )
        },
        onMarkArrival = {
            viewModel.addJourneyEvent(
                title = arrivedTitle,
                description = arrivedDescription
            )
        },
        onShowBirthSheet = { viewModel.showBirthSheet(state.laborSummary) },
        onHideBirthSheet = viewModel::hideBirthSheet,
        onBabyNameChanged = viewModel::updateBabyName,
        onWeightChanged = viewModel::updateWeight,
        onHeightChanged = viewModel::updateHeight,
        onConfirmBirth = {
            viewModel.markBirth(
                eventTitle = birthTitle
            )
        },
        onSkipBirth = {
            viewModel.markBirthWithoutDetails(
                eventTitle = birthTitle
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventsContent(
    state: EventsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)?,
    onOpenContraction: () -> Unit,
    onOpenWaterBreak: () -> Unit,
    onOpenDecision: () -> Unit,
    onOpenLaborDetails: () -> Unit,
    onMarkLaborStarted: () -> Unit,
    onMarkDeparture: () -> Unit,
    onMarkArrival: () -> Unit,
    onShowBirthSheet: () -> Unit,
    onHideBirthSheet: () -> Unit,
    onBabyNameChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onHeightChanged: (String) -> Unit,
    onConfirmBirth: () -> Unit,
    onSkipBirth: () -> Unit
) {
    val spacing = DadTheme.spacing

    ScreenScaffold(
        title = stringResource(id = R.string.events_title),
        subtitle = stringResource(id = R.string.events_subtitle),
        onBack = onBack,
        onMenu = onMenu,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    StatusCard(
                        title = stringResource(id = stageTitleRes(state.appStage)),
                        description = stringResource(
                            id = when (state.appStage) {
                                AppStage.PREPARING -> R.string.events_stage_preparing_description
                                AppStage.LABOR -> R.string.events_stage_labor_description
                                AppStage.AFTER_BIRTH -> R.string.events_stage_after_birth_description
                            }
                        ),
                        tone = when (state.appStage) {
                            AppStage.PREPARING -> StatusTone.Calm
                            AppStage.LABOR -> StatusTone.Warning
                            AppStage.AFTER_BIRTH -> StatusTone.Success
                        },
                        icon = when (state.appStage) {
                            AppStage.PREPARING -> Icons.Outlined.CheckCircle
                            AppStage.LABOR -> Icons.Outlined.MonitorHeart
                            AppStage.AFTER_BIRTH -> Icons.Outlined.ChildCare
                        },
                        headline = stringResource(id = R.string.events_stage_overline)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            if (state.hasActiveContractionSession && state.appStage != AppStage.AFTER_BIRTH) {
                                SecondaryButton(
                                    text = stringResource(id = R.string.dashboard_open_contraction_cta),
                                    onClick = onOpenContraction,
                                    icon = Icons.Outlined.AccessTime
                                )
                            }
                            if (state.hasActiveWaterBreak) {
                                SecondaryButton(
                                    text = stringResource(id = R.string.dashboard_water_action),
                                    onClick = onOpenWaterBreak,
                                    icon = Icons.Outlined.WaterDrop
                                )
                            }
                        }
                    }
                }

                item {
                    InfoCard(
                        title = stringResource(id = R.string.events_main_tools_title),
                        description = stringResource(id = R.string.events_main_tools_description),
                        icon = Icons.Outlined.LocalHospital
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                            if (state.appStage != AppStage.AFTER_BIRTH) {
                                PrimaryButton(
                                    text = stringResource(id = R.string.action_contraction_counter),
                                    onClick = onOpenContraction,
                                    icon = Icons.Outlined.MonitorHeart
                                )
                            }
                            SecondaryButton(
                                text = stringResource(id = R.string.action_water_break_timer),
                                onClick = onOpenWaterBreak,
                                icon = Icons.Outlined.WaterDrop
                            )
                            SecondaryButton(
                                text = stringResource(id = R.string.action_when_go_hospital),
                                onClick = onOpenDecision,
                                icon = Icons.Outlined.LocalHospital
                            )
                        }
                    }
                }

                if (state.appStage == AppStage.PREPARING) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.events_action_labor_started),
                            description = stringResource(id = R.string.events_action_labor_started_desc),
                            icon = Icons.Outlined.MonitorHeart
                        ) {
                            PrimaryButton(
                                text = stringResource(id = R.string.events_action_labor_started),
                                onClick = onMarkLaborStarted
                            )
                        }
                    }
                }

                if (state.appStage == AppStage.LABOR) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.events_logistics_title),
                            description = stringResource(id = R.string.events_logistics_description),
                            icon = Icons.Outlined.DirectionsCar
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                SecondaryButton(
                                    text = stringResource(id = R.string.events_action_departed),
                                    onClick = onMarkDeparture,
                                    icon = Icons.Outlined.DirectionsCar
                                )
                                SecondaryButton(
                                    text = stringResource(id = R.string.events_action_arrived),
                                    onClick = onMarkArrival,
                                    icon = Icons.Outlined.LocalHospital
                                )
                                PrimaryButton(
                                    text = stringResource(id = R.string.events_action_birth),
                                    onClick = onShowBirthSheet,
                                    icon = Icons.Outlined.ChildCare
                                )
                            }
                        }
                    }
                }

                if (state.appStage == AppStage.AFTER_BIRTH || state.laborSummary.birthTime != null) {
                    item {
                        InfoCard(
                            title = stringResource(id = R.string.events_birth_summary_title),
                            description = state.laborSummary.birthTime?.toReadableDateTime()
                                ?: stringResource(id = R.string.events_birth_summary_empty),
                            icon = Icons.Outlined.BabyChangingStation,
                            overline = stringResource(id = R.string.events_birth_summary_overline)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                if (!state.laborSummary.babyName.isNullOrBlank()) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.events_birth_name_value,
                                            state.laborSummary.babyName.orEmpty()
                                        ),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                if (state.laborSummary.birthWeightGrams != null || state.laborSummary.birthHeightCm != null) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.events_birth_metrics_value,
                                            state.laborSummary.birthWeightGrams?.toString()
                                                ?: stringResource(id = R.string.unknown),
                                            state.laborSummary.birthHeightCm?.toString()
                                                ?: stringResource(id = R.string.unknown)
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                SecondaryButton(
                                    text = stringResource(id = R.string.events_open_birth_details),
                                    onClick = onOpenLaborDetails
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showBirthSheet) {
        ModalBottomSheet(
            onDismissRequest = onHideBirthSheet,
            shape = DadTheme.shapes.sheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    text = stringResource(id = R.string.events_birth_sheet_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(id = R.string.events_birth_sheet_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.babyNameInput,
                    onValueChange = onBabyNameChanged,
                    label = { Text(text = stringResource(id = R.string.events_birth_name_hint)) }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.weightInput,
                    onValueChange = onWeightChanged,
                    label = { Text(text = stringResource(id = R.string.birth_weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.heightInput,
                    onValueChange = onHeightChanged,
                    label = { Text(text = stringResource(id = R.string.birth_height)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                PrimaryButton(
                    text = stringResource(id = R.string.events_birth_confirm),
                    onClick = onConfirmBirth
                )
                SecondaryButton(
                    text = stringResource(id = R.string.events_birth_skip),
                    onClick = onSkipBirth
                )
            }
        }
    }
}

private fun stageTitleRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.LABOR -> R.string.app_stage_labor
    AppStage.AFTER_BIRTH -> R.string.app_stage_after_birth
}
