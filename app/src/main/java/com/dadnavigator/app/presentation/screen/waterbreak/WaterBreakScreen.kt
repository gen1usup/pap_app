package com.dadnavigator.app.presentation.screen.waterbreak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone
import java.time.Duration
import java.time.Instant

@Composable
fun WaterBreakScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: WaterBreakViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.errorRes?.let { stringResource(id = it) }
        ?: state.infoRes?.let { stringResource(id = it) }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    WaterBreakContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onColorSelected = viewModel::setColor,
        onNotesChanged = viewModel::setNotes,
        onSave = viewModel::saveEvent,
        onCloseActive = viewModel::closeActiveEvent,
        onDeleteEvent = viewModel::deleteEvent
    )
}

@Composable
private fun WaterBreakContent(
    state: WaterBreakUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onColorSelected: (WaterColor) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCloseActive: () -> Unit,
    onDeleteEvent: (Long) -> Unit
) {
    val spacing = DadTheme.spacing
    var pendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    ScreenScaffold(
        title = stringResource(id = R.string.water_break_title),
        subtitle = stringResource(id = R.string.water_break_subtitle),
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
                    StatusCard(
                        title = if (state.activeEvent != null) {
                            stringResource(id = R.string.water_break_active_title)
                        } else {
                            stringResource(id = R.string.water_break_idle_title)
                        },
                        description = if (state.activeEvent != null) {
                            stringResource(
                                id = R.string.dashboard_water_active_description,
                                state.elapsed.toReadableDuration()
                            )
                        } else {
                            stringResource(id = R.string.water_break_warning_short)
                        },
                        tone = if (state.activeEvent != null) StatusTone.Warning else StatusTone.Calm,
                        icon = Icons.Outlined.WaterDrop,
                        headline = stringResource(id = R.string.water_break_overline)
                    ) {
                        if (state.activeEvent != null) {
                            Text(
                                text = stringResource(
                                    id = R.string.water_break_event_details,
                                    state.activeEvent.happenedAt.toReadableDateTime(),
                                    stringResource(id = colorLabelRes(state.activeEvent.color))
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            SecondaryButton(
                                text = stringResource(id = R.string.close_active_water_break),
                                onClick = onCloseActive
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = DadTheme.shapes.card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            Text(
                                text = stringResource(id = R.string.record_water_break),
                                style = MaterialTheme.typography.titleLarge
                            )
                            RowColorChips(
                                selectedColor = state.selectedColor,
                                onSelected = onColorSelected
                            )
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.notes,
                                onValueChange = onNotesChanged,
                                minLines = 3,
                                label = { Text(text = stringResource(id = R.string.water_break_notes)) }
                            )
                            PrimaryButton(
                                text = stringResource(id = R.string.save_event),
                                onClick = onSave
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = DadTheme.shapes.card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Text(
                                text = stringResource(id = R.string.water_break_warning_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = stringResource(id = R.string.water_break_warning_long),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(id = R.string.water_break_history),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (state.history.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.empty_state_title),
                            description = stringResource(id = R.string.no_water_break_history),
                            icon = Icons.Outlined.Alarm
                        )
                    }
                } else {
                    items(state.history, key = { it.id }) { event ->
                        Card(
                            shape = DadTheme.shapes.card,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.happenedAt.toReadableDateTime(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    IconButton(onClick = { pendingDeleteId = event.id }) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = stringResource(id = R.string.action_delete)
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(id = colorLabelRes(event.color)),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (event.notes.isNotBlank()) {
                                    Text(
                                        text = event.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(text = stringResource(id = R.string.delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.water_break_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val eventId = pendingDeleteId ?: return@TextButton
                        pendingDeleteId = null
                        onDeleteEvent(eventId)
                    }
                ) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RowColorChips(selectedColor: WaterColor, onSelected: (WaterColor) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
    ) {
        WaterColor.entries.forEach { color ->
            FilterChip(
                selected = selectedColor == color,
                onClick = { onSelected(color) },
                label = { Text(text = stringResource(id = colorLabelRes(color))) }
            )
        }
    }
}

private fun colorLabelRes(color: WaterColor): Int = when (color) {
    WaterColor.CLEAR -> R.string.water_color_clear
    WaterColor.PINK -> R.string.water_color_pink
    WaterColor.GREEN -> R.string.water_color_green
    WaterColor.BROWN -> R.string.water_color_brown
}

@Preview(showBackground = true)
@Composable
private fun WaterBreakPreview() {
    DadNavigatorTheme(dynamicColor = false) {
        WaterBreakContent(
            state = WaterBreakUiState(
                activeEvent = WaterBreakEvent(1, "u", Instant.now(), WaterColor.CLEAR, "Без запаха", null),
                history = listOf(
                    WaterBreakEvent(2, "u", Instant.now().minusSeconds(3600), WaterColor.PINK, "", Instant.now())
                ),
                elapsed = Duration.ofMinutes(42)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onColorSelected = {},
            onNotesChanged = {},
            onSave = {},
            onCloseActive = {},
            onDeleteEvent = {}
        )
    }
}
