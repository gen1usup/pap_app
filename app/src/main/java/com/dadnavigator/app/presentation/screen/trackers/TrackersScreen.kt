package com.dadnavigator.app.presentation.screen.trackers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.core.util.toReadableDuration
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Postpartum trackers screen.
 */
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

    ScreenScaffold(
        title = stringResource(id = R.string.trackers_title),
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScrollableTabRow(selectedTabIndex = state.selectedTab.ordinal) {
                    Tab(
                        selected = state.selectedTab == TrackerTab.FEEDING,
                        onClick = { viewModel.setTab(TrackerTab.FEEDING) },
                        text = { Text(text = stringResource(id = R.string.tracker_feeding)) }
                    )
                    Tab(
                        selected = state.selectedTab == TrackerTab.DIAPER,
                        onClick = { viewModel.setTab(TrackerTab.DIAPER) },
                        text = { Text(text = stringResource(id = R.string.tracker_diaper)) }
                    )
                    Tab(
                        selected = state.selectedTab == TrackerTab.SLEEP,
                        onClick = { viewModel.setTab(TrackerTab.SLEEP) },
                        text = { Text(text = stringResource(id = R.string.tracker_sleep)) }
                    )
                    Tab(
                        selected = state.selectedTab == TrackerTab.NOTES,
                        onClick = { viewModel.setTab(TrackerTab.NOTES) },
                        text = { Text(text = stringResource(id = R.string.tracker_notes)) }
                    )
                }
            }

            when (state.selectedTab) {
                TrackerTab.FEEDING -> {
                    item {
                        FeedingForm(
                            state = state,
                            onDurationChange = viewModel::setFeedingDuration,
                            onTypeSelect = viewModel::setFeedingType,
                            onAdd = viewModel::addFeeding
                        )
                    }
                    if (state.feedingLogs.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(id = R.string.empty_state_title),
                                description = stringResource(id = R.string.empty_state_description)
                            )
                        }
                    } else {
                        items(state.feedingLogs) { log ->
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = log.timestamp.toReadableDateTime(), style = MaterialTheme.typography.titleMedium)
                                    Text(text = stringResource(id = feedingTypeRes(log.type)), style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = stringResource(id = R.string.minutes_format, log.durationMinutes),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                TrackerTab.DIAPER -> {
                    item {
                        DiaperForm(
                            state = state,
                            onTypeSelect = viewModel::setDiaperType,
                            onNotesChange = viewModel::setDiaperNotes,
                            onAdd = viewModel::addDiaper
                        )
                    }
                    if (state.diaperLogs.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(id = R.string.empty_state_title),
                                description = stringResource(id = R.string.empty_state_description)
                            )
                        }
                    } else {
                        items(state.diaperLogs) { log ->
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = log.timestamp.toReadableDateTime(), style = MaterialTheme.typography.titleMedium)
                                    Text(text = stringResource(id = diaperTypeRes(log.type)), style = MaterialTheme.typography.bodyLarge)
                                    if (log.notes.isNotBlank()) {
                                        Text(text = log.notes, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                TrackerTab.SLEEP -> {
                    item {
                        SleepForm(
                            state = state,
                            onDurationChange = viewModel::setSleepDuration,
                            onNotesChange = viewModel::setSleepNotes,
                            onAdd = viewModel::addSleep
                        )
                    }
                    if (state.sleepLogs.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(id = R.string.empty_state_title),
                                description = stringResource(id = R.string.empty_state_description)
                            )
                        }
                    } else {
                        items(state.sleepLogs) { log ->
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = log.startTime.toReadableDateTime(), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = log.duration.toReadableDuration(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (log.notes.isNotBlank()) {
                                        Text(text = log.notes, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                TrackerTab.NOTES -> {
                    item {
                        NotesForm(
                            state = state,
                            onNoteChange = viewModel::setNoteText,
                            onAdd = viewModel::addNote
                        )
                    }
                    if (state.notes.isEmpty()) {
                        item {
                            EmptyState(
                                title = stringResource(id = R.string.empty_state_title),
                                description = stringResource(id = R.string.empty_state_description)
                            )
                        }
                    } else {
                        items(state.notes) { note ->
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = note.timestamp.toReadableDateTime(), style = MaterialTheme.typography.titleMedium)
                                    Text(text = note.text, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

@Composable
private fun FeedingForm(
    state: TrackersUiState,
    onDurationChange: (String) -> Unit,
    onTypeSelect: (FeedingType) -> Unit,
    onAdd: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.feedingDurationInput,
                onValueChange = onDurationChange,
                label = { Text(text = stringResource(id = R.string.duration_minutes)) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedingType.entries.forEach { type ->
                    FilterChip(
                        selected = type == state.feedingType,
                        onClick = { onTypeSelect(type) },
                        label = { Text(text = stringResource(id = feedingTypeRes(type))) }
                    )
                }
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onAdd) {
                Text(text = stringResource(id = R.string.add_log))
            }
        }
    }
}

@Composable
private fun DiaperForm(
    state: TrackersUiState,
    onTypeSelect: (DiaperType) -> Unit,
    onNotesChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Button(modifier = Modifier.fillMaxWidth(), onClick = onAdd) {
                Text(text = stringResource(id = R.string.add_log))
            }
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
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
            Button(modifier = Modifier.fillMaxWidth(), onClick = onAdd) {
                Text(text = stringResource(id = R.string.add_log))
            }
        }
    }
}

@Composable
private fun NotesForm(
    state: TrackersUiState,
    onNoteChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.noteInput,
                onValueChange = onNoteChange,
                label = { Text(text = stringResource(id = R.string.note_hint)) }
            )
            Button(modifier = Modifier.fillMaxWidth(), onClick = onAdd) {
                Text(text = stringResource(id = R.string.add_log))
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

