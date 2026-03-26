package com.dadnavigator.app.presentation.screen.labor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Screen for labor phase record keeping.
 */
@Composable
fun LaborScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: LaborViewModel = hiltViewModel()
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
        title = stringResource(id = R.string.labor_title),
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
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = stringResource(id = R.string.labor_start_time), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = state.summary.laborStartTime?.toReadableDateTime() ?: stringResource(id = R.string.unknown),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::markLaborStartNow
                        ) {
                            Text(text = stringResource(id = R.string.labor_mark_now))
                        }

                        Text(text = stringResource(id = R.string.birth_time), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = state.summary.birthTime?.toReadableDateTime() ?: stringResource(id = R.string.unknown),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::markBirthNow
                        ) {
                            Text(text = stringResource(id = R.string.labor_mark_now))
                        }

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.weightInput,
                            onValueChange = viewModel::updateWeight,
                            label = { Text(text = stringResource(id = R.string.birth_weight)) }
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.heightInput,
                            onValueChange = viewModel::updateHeight,
                            label = { Text(text = stringResource(id = R.string.birth_height)) }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::saveSummary
                        ) {
                            Text(text = stringResource(id = R.string.save_labor_info))
                        }
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.customEventTitle,
                            onValueChange = viewModel::updateEventTitle,
                            label = { Text(text = stringResource(id = R.string.labor_event_hint)) }
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.customEventNote,
                            onValueChange = viewModel::updateEventNote,
                            label = { Text(text = stringResource(id = R.string.labor_note_hint)) }
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = viewModel::addCustomEvent
                        ) {
                            Text(text = stringResource(id = R.string.add_timeline_event))
                        }
                    }
                }
            }

            item {
                Text(text = stringResource(id = R.string.timeline_title), style = MaterialTheme.typography.titleMedium)
            }

            if (state.laborEvents.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(id = R.string.empty_state_title),
                        description = stringResource(id = R.string.no_timeline)
                    )
                }
            } else {
                items(state.laborEvents) { event ->
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = event.title.ifBlank { stringResource(id = R.string.timeline_type_labor) },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = event.timestamp.toReadableDateTime(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (event.description.isNotBlank()) {
                                Text(text = event.description, style = MaterialTheme.typography.bodyLarge)
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
