package com.dadnavigator.app.presentation.screen.waterbreak

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Water break timer screen.
 */
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
    val errorMessage = state.errorRes?.let { stringResource(id = it) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.dismissError()
        }
    }

    ScreenScaffold(
        title = stringResource(id = R.string.water_break_title),
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.water_break_warning_short),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(id = R.string.water_break_warning_long),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                if (state.activeEvent != null) {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = stringResource(id = R.string.active_event), style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${stringResource(id = R.string.water_break_time)}: ${state.activeEvent.happenedAt.toReadableDateTime()}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${stringResource(id = R.string.water_break_elapsed)}: ${state.elapsed.toReadableDuration()}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${stringResource(id = R.string.water_break_color)}: ${stringResource(id = colorLabelRes(state.activeEvent.color))}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (state.activeEvent.notes.isNotBlank()) {
                                Text(text = state.activeEvent.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = viewModel::closeActiveEvent
                            ) {
                                Text(text = stringResource(id = R.string.close_active_water_break))
                            }
                        }
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = stringResource(id = R.string.record_water_break), style = MaterialTheme.typography.titleMedium)
                        RowColorChips(
                            selectedColor = state.selectedColor,
                            onSelected = viewModel::setColor
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.notes,
                            onValueChange = viewModel::setNotes,
                            label = { Text(text = stringResource(id = R.string.water_break_notes)) }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::saveEvent
                        ) {
                            Text(text = stringResource(id = R.string.save_event))
                        }
                    }
                }
            }

            item {
                Text(text = stringResource(id = R.string.water_break_history), style = MaterialTheme.typography.titleMedium)
            }

            if (state.history.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(id = R.string.empty_state_title),
                        description = stringResource(id = R.string.no_water_break_history)
                    )
                }
            } else {
                items(state.history) { event ->
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = event.happenedAt.toReadableDateTime(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${stringResource(id = R.string.water_break_color)}: ${stringResource(id = colorLabelRes(event.color))}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (event.notes.isNotBlank()) {
                                Text(text = event.notes, style = MaterialTheme.typography.bodyMedium)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RowColorChips(selectedColor: WaterColor, onSelected: (WaterColor) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
