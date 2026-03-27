package com.dadnavigator.app.presentation.screen.events

import androidx.annotation.StringRes
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
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NightlightRound
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.service.EventAction
import com.dadnavigator.app.domain.service.EventsSection
import com.dadnavigator.app.domain.service.EventsSectionType
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone
import com.dadnavigator.app.presentation.component.TimelineActionButton

@Composable
fun EventsScreen(
    userId: String,
    onOpenContraction: () -> Unit,
    onOpenWaterBreak: () -> Unit,
    onOpenDecision: () -> Unit,
    onOpenLaborDetails: () -> Unit,
    onBack: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
    onOpenTimeline: (() -> Unit)? = null,
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
    val homeTitle = stringResource(id = R.string.events_action_arrived_home)
    val homeDescription = stringResource(id = R.string.events_action_arrived_home_desc)

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
        onOpenTimeline = onOpenTimeline,
        onAction = { action ->
            when (action) {
                EventAction.OpenContractionTimer -> onOpenContraction()
                EventAction.OpenWaterBreakTimer -> onOpenWaterBreak()
                EventAction.OpenDecisionHelp -> onOpenDecision()
                EventAction.OpenBirthDetails -> onOpenLaborDetails()
                EventAction.MarkLaborStarted -> viewModel.markLaborStarted(
                    eventTitle = laborStartedTitle,
                    eventDescription = laborStartedDescription
                )
                EventAction.StartContraction,
                EventAction.StopContraction -> viewModel.toggleContraction()
                EventAction.MarkLeftHome -> viewModel.addJourneyEvent(
                    title = departedTitle,
                    description = departedDescription
                )
                EventAction.MarkArrivedHospital -> viewModel.addJourneyEvent(
                    title = arrivedTitle,
                    description = arrivedDescription
                )
                EventAction.ShowBirthSheet -> viewModel.showBirthSheet(state.laborSummary)
                EventAction.MarkArrivedHome -> viewModel.markArrivedHome(
                    eventTitle = homeTitle,
                    eventDescription = homeDescription
                )
                EventAction.RecordBagReady,
                EventAction.RecordTestDrive,
                EventAction.RecordPreparationNote,
                EventAction.RecordLaborNote,
                EventAction.RecordHospitalNote,
                EventAction.RecordSupportAction,
                EventAction.RecordPhotoNote,
                EventAction.RecordFeeding,
                EventAction.RecordSleep,
                EventAction.RecordDiaper,
                EventAction.RecordTemperature,
                EventAction.RecordWeight,
                EventAction.RecordHomeNote -> viewModel.showQuickRecordSheet(action)
            }
        },
        onHideBirthSheet = viewModel::hideBirthSheet,
        onBabyNameChanged = viewModel::updateBabyName,
        onWeightChanged = viewModel::updateWeight,
        onHeightChanged = viewModel::updateHeight,
        onConfirmBirth = {
            viewModel.markBirth(eventTitle = birthTitle)
        },
        onSkipBirth = {
            viewModel.markBirthWithoutDetails(eventTitle = birthTitle)
        },
        onHideQuickRecordSheet = viewModel::hideQuickRecordSheet,
        onQuickRecordTitleChanged = viewModel::updateQuickRecordTitle,
        onQuickRecordDescriptionChanged = viewModel::updateQuickRecordDescription,
        onSaveQuickRecord = viewModel::saveQuickRecord
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventsContent(
    state: EventsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)?,
    onOpenTimeline: (() -> Unit)?,
    onAction: (EventAction) -> Unit,
    onHideBirthSheet: () -> Unit,
    onBabyNameChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onHeightChanged: (String) -> Unit,
    onConfirmBirth: () -> Unit,
    onSkipBirth: () -> Unit,
    onHideQuickRecordSheet: () -> Unit,
    onQuickRecordTitleChanged: (String) -> Unit,
    onQuickRecordDescriptionChanged: (String) -> Unit,
    onSaveQuickRecord: () -> Unit
) {
    val spacing = DadTheme.spacing

    ScreenScaffold(
        title = stringResource(id = R.string.events_title),
        subtitle = stringResource(id = R.string.events_subtitle),
        onBack = onBack,
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
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    StatusCard(
                        title = stringResource(id = stageTitleRes(state.appStage)),
                        description = stringResource(id = stageDescriptionRes(state.appStage)),
                        tone = stageTone(state.appStage),
                        icon = stageIcon(state.appStage),
                        headline = stringResource(id = R.string.events_stage_overline)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            if (state.isContractionRunning) {
                                Text(
                                    text = stringResource(id = R.string.events_status_contraction_running),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else if (state.appStage == AppStage.CONTRACTIONS) {
                                Text(
                                    text = stringResource(id = R.string.events_status_contraction_waiting),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (state.hasActiveWaterBreak) {
                                Text(
                                    text = stringResource(id = R.string.events_status_water_break_running),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                state.content.sections.forEach { section ->
                    item {
                        EventsSectionCard(
                            section = section,
                            onAction = onAction
                        )
                    }
                }

                if (state.content.showBirthSummary) {
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
                                    onClick = { onAction(EventAction.OpenBirthDetails) }
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

    val quickRecordAction = state.activeQuickRecordAction
    if (state.showQuickRecordSheet && quickRecordAction != null) {
        ModalBottomSheet(
            onDismissRequest = onHideQuickRecordSheet,
            shape = DadTheme.shapes.sheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    text = stringResource(id = quickRecordSheetTitleRes(quickRecordAction)),
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(id = quickRecordSheetDescriptionRes(quickRecordAction)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (quickRecordTitleEditable(quickRecordAction)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.quickRecordTitleInput,
                        onValueChange = onQuickRecordTitleChanged,
                        label = { Text(text = stringResource(id = R.string.timeline_title_field)) }
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.quickRecordDescriptionInput,
                    onValueChange = onQuickRecordDescriptionChanged,
                    minLines = 3,
                    label = { Text(text = stringResource(id = R.string.events_quick_record_note_label)) }
                )
                PrimaryButton(
                    text = stringResource(id = R.string.save_event),
                    onClick = onSaveQuickRecord
                )
                SecondaryButton(
                    text = stringResource(id = R.string.cancel),
                    onClick = onHideQuickRecordSheet
                )
            }
        }
    }
}

@Composable
private fun EventsSectionCard(
    section: EventsSection,
    onAction: (EventAction) -> Unit
) {
    val spacing = DadTheme.spacing
    val metadata = sectionMetadata(section.type)

    InfoCard(
        title = stringResource(id = metadata.titleRes),
        description = stringResource(id = metadata.descriptionRes),
        icon = metadata.icon
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            section.actions.forEach { action ->
                if (actionIsPrimary(action)) {
                    PrimaryButton(
                        text = stringResource(id = actionLabelRes(action)),
                        onClick = { onAction(action) },
                        icon = actionIcon(action)
                    )
                } else {
                    SecondaryButton(
                        text = stringResource(id = actionLabelRes(action)),
                        onClick = { onAction(action) },
                        icon = actionIcon(action)
                    )
                }
            }
        }
    }
}

private data class SectionMetadata(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector
)

private fun sectionMetadata(type: EventsSectionType): SectionMetadata = when (type) {
    EventsSectionType.PreparationTools -> SectionMetadata(
        titleRes = R.string.events_section_preparation_tools_title,
        descriptionRes = R.string.events_section_preparation_tools_description,
        icon = Icons.Outlined.CheckCircle
    )
    EventsSectionType.ReadinessWindow -> SectionMetadata(
        titleRes = R.string.events_section_readiness_title,
        descriptionRes = R.string.events_section_readiness_description,
        icon = Icons.Outlined.LocalHospital
    )
    EventsSectionType.PreparationRecords -> SectionMetadata(
        titleRes = R.string.events_section_preparation_records_title,
        descriptionRes = R.string.events_section_preparation_records_description,
        icon = Icons.Outlined.EditNote
    )
    EventsSectionType.LiveLaborActions -> SectionMetadata(
        titleRes = R.string.events_section_live_labor_title,
        descriptionRes = R.string.events_section_live_labor_description,
        icon = Icons.Outlined.MonitorHeart
    )
    EventsSectionType.LaborLogistics -> SectionMetadata(
        titleRes = R.string.events_section_logistics_title,
        descriptionRes = R.string.events_section_logistics_description,
        icon = Icons.Outlined.DirectionsCar
    )
    EventsSectionType.HospitalActions -> SectionMetadata(
        titleRes = R.string.events_section_hospital_title,
        descriptionRes = R.string.events_section_hospital_description,
        icon = Icons.Outlined.BabyChangingStation
    )
    EventsSectionType.HomeTrackers -> SectionMetadata(
        titleRes = R.string.events_section_home_trackers_title,
        descriptionRes = R.string.events_section_home_trackers_description,
        icon = Icons.Outlined.ChildCare
    )
    EventsSectionType.HomeNotes -> SectionMetadata(
        titleRes = R.string.events_section_home_notes_title,
        descriptionRes = R.string.events_section_home_notes_description,
        icon = Icons.Outlined.EditNote
    )
}

private fun actionIsPrimary(action: EventAction): Boolean = when (action) {
    EventAction.MarkLaborStarted,
    EventAction.StartContraction,
    EventAction.StopContraction,
    EventAction.ShowBirthSheet,
    EventAction.MarkArrivedHome,
    EventAction.RecordFeeding -> true
    else -> false
}

private fun actionLabelRes(action: EventAction): Int = when (action) {
    EventAction.OpenContractionTimer -> R.string.action_contraction_counter
    EventAction.OpenWaterBreakTimer -> R.string.action_water_break_timer
    EventAction.OpenDecisionHelp -> R.string.action_when_go_hospital
    EventAction.OpenBirthDetails -> R.string.events_open_birth_details
    EventAction.MarkLaborStarted -> R.string.events_action_labor_started
    EventAction.StartContraction -> R.string.start_contraction
    EventAction.StopContraction -> R.string.stop_contraction
    EventAction.MarkLeftHome -> R.string.events_action_departed
    EventAction.MarkArrivedHospital -> R.string.events_action_arrived
    EventAction.ShowBirthSheet -> R.string.events_action_birth
    EventAction.MarkArrivedHome -> R.string.events_action_arrived_home
    EventAction.RecordBagReady -> R.string.events_action_bag_ready
    EventAction.RecordTestDrive -> R.string.events_action_test_drive
    EventAction.RecordPreparationNote -> R.string.events_action_note
    EventAction.RecordLaborNote -> R.string.events_action_note
    EventAction.RecordHospitalNote -> R.string.events_action_note
    EventAction.RecordSupportAction -> R.string.events_action_support
    EventAction.RecordPhotoNote -> R.string.events_action_photo
    EventAction.RecordFeeding -> R.string.events_action_feeding
    EventAction.RecordSleep -> R.string.events_action_sleep
    EventAction.RecordDiaper -> R.string.events_action_diaper
    EventAction.RecordTemperature -> R.string.events_action_temperature
    EventAction.RecordWeight -> R.string.events_action_weight
    EventAction.RecordHomeNote -> R.string.events_action_note
}

private fun actionIcon(action: EventAction): ImageVector = when (action) {
    EventAction.OpenContractionTimer,
    EventAction.StartContraction,
    EventAction.StopContraction,
    EventAction.MarkLaborStarted -> Icons.Outlined.MonitorHeart
    EventAction.OpenWaterBreakTimer -> Icons.Outlined.WaterDrop
    EventAction.OpenDecisionHelp,
    EventAction.MarkArrivedHospital -> Icons.Outlined.LocalHospital
    EventAction.OpenBirthDetails,
    EventAction.ShowBirthSheet,
    EventAction.RecordDiaper -> Icons.Outlined.BabyChangingStation
    EventAction.MarkLeftHome,
    EventAction.MarkArrivedHome,
    EventAction.RecordTestDrive -> Icons.Outlined.DirectionsCar
    EventAction.RecordBagReady -> Icons.Outlined.CheckCircle
    EventAction.RecordPreparationNote,
    EventAction.RecordLaborNote,
    EventAction.RecordHospitalNote,
    EventAction.RecordTemperature,
    EventAction.RecordWeight,
    EventAction.RecordHomeNote -> Icons.Outlined.EditNote
    EventAction.RecordSupportAction,
    EventAction.RecordFeeding -> Icons.Outlined.FavoriteBorder
    EventAction.RecordPhotoNote -> Icons.Outlined.EditNote
    EventAction.RecordSleep -> Icons.Outlined.NightlightRound
}

private fun stageTitleRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.CONTRACTIONS -> R.string.app_stage_contractions
    AppStage.AT_HOSPITAL -> R.string.app_stage_at_hospital
    AppStage.AT_HOME -> R.string.app_stage_at_home
}

private fun stageDescriptionRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.events_stage_preparing_description
    AppStage.CONTRACTIONS -> R.string.events_stage_contractions_description
    AppStage.AT_HOSPITAL -> R.string.events_stage_at_hospital_description
    AppStage.AT_HOME -> R.string.events_stage_at_home_description
}

private fun stageTone(stage: AppStage): StatusTone = when (stage) {
    AppStage.PREPARING -> StatusTone.Calm
    AppStage.CONTRACTIONS -> StatusTone.Warning
    AppStage.AT_HOSPITAL,
    AppStage.AT_HOME -> StatusTone.Success
}

private fun stageIcon(stage: AppStage): ImageVector = when (stage) {
    AppStage.PREPARING -> Icons.Outlined.CheckCircle
    AppStage.CONTRACTIONS -> Icons.Outlined.MonitorHeart
    AppStage.AT_HOSPITAL -> Icons.Outlined.LocalHospital
    AppStage.AT_HOME -> Icons.Outlined.ChildCare
}

private fun quickRecordTitleEditable(action: EventAction): Boolean = when (action) {
    EventAction.RecordPreparationNote,
    EventAction.RecordLaborNote,
    EventAction.RecordHospitalNote,
    EventAction.RecordHomeNote -> true
    else -> false
}

private fun quickRecordSheetTitleRes(action: EventAction): Int = when (action) {
    EventAction.RecordBagReady -> R.string.events_quick_record_bag_title
    EventAction.RecordTestDrive -> R.string.events_quick_record_test_drive_title
    EventAction.RecordPreparationNote -> R.string.events_quick_record_note_title
    EventAction.RecordLaborNote -> R.string.events_quick_record_note_title
    EventAction.RecordHospitalNote -> R.string.events_quick_record_note_title
    EventAction.RecordSupportAction -> R.string.events_quick_record_support_title
    EventAction.RecordPhotoNote -> R.string.events_quick_record_photo_title
    EventAction.RecordFeeding -> R.string.events_quick_record_feeding_title
    EventAction.RecordSleep -> R.string.events_quick_record_sleep_title
    EventAction.RecordDiaper -> R.string.events_quick_record_diaper_title
    EventAction.RecordTemperature -> R.string.events_quick_record_temperature_title
    EventAction.RecordWeight -> R.string.events_quick_record_weight_title
    EventAction.RecordHomeNote -> R.string.events_quick_record_note_title
    else -> R.string.events_quick_record_note_title
}

private fun quickRecordSheetDescriptionRes(action: EventAction): Int = when (action) {
    EventAction.RecordBagReady -> R.string.events_quick_record_bag_description
    EventAction.RecordTestDrive -> R.string.events_quick_record_test_drive_description
    EventAction.RecordPreparationNote,
    EventAction.RecordLaborNote,
    EventAction.RecordHospitalNote,
    EventAction.RecordHomeNote -> R.string.events_quick_record_note_description
    EventAction.RecordSupportAction -> R.string.events_quick_record_support_description
    EventAction.RecordPhotoNote -> R.string.events_quick_record_photo_description
    EventAction.RecordFeeding -> R.string.events_quick_record_feeding_description
    EventAction.RecordSleep -> R.string.events_quick_record_sleep_description
    EventAction.RecordDiaper -> R.string.events_quick_record_diaper_description
    EventAction.RecordTemperature -> R.string.events_quick_record_temperature_description
    EventAction.RecordWeight -> R.string.events_quick_record_weight_description
    else -> R.string.events_quick_record_note_description
}
