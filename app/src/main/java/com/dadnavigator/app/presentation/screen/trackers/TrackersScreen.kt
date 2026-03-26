package com.dadnavigator.app.presentation.screen.trackers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.core.util.toReadableDuration
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@Composable
fun TrackersScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: TrackersViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.errorRes?.let { stringResource(id = it) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.dismissError()
        }
    }

    TrackersContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onTabSelected = viewModel::setTab,
        onDurationChange = viewModel::setFeedingDuration,
        onFeedingTypeSelect = viewModel::setFeedingType,
        onAddFeeding = viewModel::addFeeding,
        onDiaperTypeSelect = viewModel::setDiaperType,
        onDiaperNotesChange = viewModel::setDiaperNotes,
        onAddDiaper = viewModel::addDiaper,
        onSleepDurationChange = viewModel::setSleepDuration,
        onSleepNotesChange = viewModel::setSleepNotes,
        onAddSleep = viewModel::addSleep,
        onNoteChange = viewModel::setNoteText,
        onAddNote = viewModel::addNote
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackersContent(
    state: TrackersUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTabSelected: (TrackerTab) -> Unit,
    onDurationChange: (String) -> Unit,
    onFeedingTypeSelect: (FeedingType) -> Unit,
    onAddFeeding: () -> Unit,
    onDiaperTypeSelect: (DiaperType) -> Unit,
    onDiaperNotesChange: (String) -> Unit,
    onAddDiaper: () -> Unit,
    onSleepDurationChange: (String) -> Unit,
    onSleepNotesChange: (String) -> Unit,
    onAddSleep: () -> Unit,
    onNoteChange: (String) -> Unit,
    onAddNote: () -> Unit
) {
    val spacing = DadTheme.spacing

    ScreenScaffold(
        title = stringResource(id = R.string.trackers_title),
        subtitle = stringResource(id = R.string.trackers_subtitle),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    horizontal = spacing.md,
                    vertical = spacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        TrackerTab.entries.forEach { tab ->
                            FilterChip(
                                selected = state.selectedTab == tab,
                                onClick = { onTabSelected(tab) },
                                label = {
                                    Text(
                                        text = stringResource(
                                            id = when (tab) {
                                                TrackerTab.FEEDING -> R.string.tracker_feeding
                                                TrackerTab.DIAPER -> R.string.tracker_diaper
                                                TrackerTab.SLEEP -> R.string.tracker_sleep
                                                TrackerTab.NOTES -> R.string.tracker_notes
                                            }
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                when (state.selectedTab) {
                    TrackerTab.FEEDING -> {
                        item {
                            FeedingForm(
                                state = state,
                                onDurationChange = onDurationChange,
                                onTypeSelect = onFeedingTypeSelect,
                                onAdd = onAddFeeding
                            )
                        }
                        if (state.feedingLogs.isEmpty()) {
                            item {
                                EmptyState(
                                    title = stringResource(id = R.string.empty_state_title),
                                    description = stringResource(id = R.string.empty_state_description),
                                    icon = Icons.Outlined.ChildCare
                                )
                            }
                        } else {
                            items(state.feedingLogs) { log ->
                                TrackerLogCard(
                                    title = stringResource(id = feedingTypeRes(log.type)),
                                    time = log.timestamp.toReadableDateTime(),
                                    description = stringResource(id = R.string.minutes_format, log.durationMinutes)
                                )
                            }
                        }
                    }
                    TrackerTab.DIAPER -> {
                        item {
                            DiaperForm(
                                state = state,
                                onTypeSelect = onDiaperTypeSelect,
                                onNotesChange = onDiaperNotesChange,
                                onAdd = onAddDiaper
                            )
                        }
                        if (state.diaperLogs.isEmpty()) {
                            item {
                                EmptyState(
                                    title = stringResource(id = R.string.empty_state_title),
                                    description = stringResource(id = R.string.empty_state_description),
                                    icon = Icons.Outlined.BabyChangingStation
                                )
                            }
                        } else {
                            items(state.diaperLogs) { log ->
                                TrackerLogCard(
                                    title = stringResource(id = diaperTypeRes(log.type)),
                                    time = log.timestamp.toReadableDateTime(),
                                    description = log.notes.takeIf { it.isNotBlank() }
                                )
                            }
                        }
                    }
                    TrackerTab.SLEEP -> {
                        item {
                            SleepForm(
                                state = state,
                                onDurationChange = onSleepDurationChange,
                                onNotesChange = onSleepNotesChange,
                                onAdd = onAddSleep
                            )
                        }
                        if (state.sleepLogs.isEmpty()) {
                            item {
                                EmptyState(
                                    title = stringResource(id = R.string.empty_state_title),
                                    description = stringResource(id = R.string.empty_state_description),
                                    icon = Icons.Outlined.Hotel
                                )
                            }
                        } else {
                            items(state.sleepLogs) { log ->
                                TrackerLogCard(
                                    title = stringResource(id = R.string.tracker_sleep),
                                    time = log.startTime.toReadableDateTime(),
                                    description = buildString {
                                        append(log.duration.toReadableDuration())
                                        if (log.notes.isNotBlank()) {
                                            append(" • ")
                                            append(log.notes)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    TrackerTab.NOTES -> {
                        item {
                            NotesForm(
                                state = state,
                                onNoteChange = onNoteChange,
                                onAdd = onAddNote
                            )
                        }
                        if (state.notes.isEmpty()) {
                            item {
                                EmptyState(
                                    title = stringResource(id = R.string.empty_state_title),
                                    description = stringResource(id = R.string.empty_state_description),
                                    icon = Icons.Outlined.EditNote
                                )
                            }
                        } else {
                            items(state.notes) { note ->
                                TrackerLogCard(
                                    title = stringResource(id = R.string.tracker_notes),
                                    time = note.timestamp.toReadableDateTime(),
                                    description = note.text
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedingForm(
    state: TrackersUiState,
    onDurationChange: (String) -> Unit,
    onTypeSelect: (FeedingType) -> Unit,
    onAdd: () -> Unit
) {
    InfoCard(
        title = stringResource(id = R.string.trackers_feeding_title),
        description = stringResource(id = R.string.trackers_feeding_description),
        icon = Icons.Outlined.ChildCare
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.feedingDurationInput,
                onValueChange = onDurationChange,
                label = { Text(text = stringResource(id = R.string.duration_minutes)) }
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
            ) {
                FeedingType.entries.forEach { type ->
                    FilterChip(
                        selected = type == state.feedingType,
                        onClick = { onTypeSelect(type) },
                        label = { Text(text = stringResource(id = feedingTypeRes(type))) }
                    )
                }
            }
            PrimaryButton(
                text = stringResource(id = R.string.add_log),
                onClick = onAdd
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiaperForm(
    state: TrackersUiState,
    onTypeSelect: (DiaperType) -> Unit,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    InfoCard(
        title = stringResource(id = R.string.trackers_diaper_title),
        description = stringResource(id = R.string.trackers_diaper_description),
        icon = Icons.Outlined.BabyChangingStation
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
            ) {
                DiaperType.entries.forEach { type ->
                    FilterChip(
                        selected = type == state.diaperType,
                        onClick = { onTypeSelect(type) },
                        label = { Text(text = stringResource(id = diaperTypeRes(type))) }
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.diaperNotesInput,
                onValueChange = onNotesChange,
                label = { Text(text = stringResource(id = R.string.water_break_notes)) }
            )
            PrimaryButton(
                text = stringResource(id = R.string.add_log),
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun SleepForm(
    state: TrackersUiState,
    onDurationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    InfoCard(
        title = stringResource(id = R.string.trackers_sleep_title),
        description = stringResource(id = R.string.trackers_sleep_description),
        icon = Icons.Outlined.Hotel
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.sleepDurationInput,
                onValueChange = onDurationChange,
                label = { Text(text = stringResource(id = R.string.duration_minutes)) }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.sleepNotesInput,
                onValueChange = onNotesChange,
                label = { Text(text = stringResource(id = R.string.water_break_notes)) }
            )
            PrimaryButton(
                text = stringResource(id = R.string.add_log),
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun NotesForm(
    state: TrackersUiState,
    onNoteChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    InfoCard(
        title = stringResource(id = R.string.trackers_note_title),
        description = stringResource(id = R.string.trackers_note_description),
        icon = Icons.Outlined.EditNote
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.noteInput,
                onValueChange = onNoteChange,
                minLines = 3,
                label = { Text(text = stringResource(id = R.string.note_hint)) }
            )
            PrimaryButton(
                text = stringResource(id = R.string.add_log),
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun TrackerLogCard(
    title: String,
    time: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(DadTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xs)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!description.isNullOrBlank()) {
                Text(text = description, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun feedingTypeRes(type: FeedingType): Int = when (type) {
    FeedingType.LEFT -> R.string.side_left
    FeedingType.RIGHT -> R.string.side_right
    FeedingType.BOTTLE -> R.string.side_bottle
}

private fun diaperTypeRes(type: DiaperType): Int = when (type) {
    DiaperType.WET -> R.string.diaper_wet
    DiaperType.DIRTY -> R.string.diaper_dirty
    DiaperType.MIXED -> R.string.diaper_mixed
}

@Preview(showBackground = true)
@Composable
private fun TrackersPreview() {
    DadNavigatorTheme(dynamicColor = false) {
        TrackersContent(
            state = TrackersUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onTabSelected = {},
            onDurationChange = {},
            onFeedingTypeSelect = {},
            onAddFeeding = {},
            onDiaperTypeSelect = {},
            onDiaperNotesChange = {},
            onAddDiaper = {},
            onSleepDurationChange = {},
            onSleepNotesChange = {},
            onAddSleep = {},
            onNoteChange = {},
            onAddNote = {}
        )
    }
}
