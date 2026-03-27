package com.dadnavigator.app.presentation.screen.labor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Timeline
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
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

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
    val message = state.errorRes?.let { stringResource(id = it) }
        ?: state.infoRes?.let { stringResource(id = it) }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    val spacing = DadTheme.spacing
    ScreenScaffold(
        title = stringResource(id = R.string.labor_title),
        subtitle = stringResource(id = R.string.labor_subtitle),
        onBack = onBack,
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
                    InfoCard(
                        title = stringResource(id = R.string.labor_title),
                        description = stringResource(id = R.string.labor_description),
                        icon = Icons.Outlined.MonitorHeart
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                            Text(
                                text = stringResource(
                                    id = R.string.labor_start_display,
                                    state.summary.laborStartTime?.toReadableDateTime()
                                        ?: stringResource(id = R.string.unknown)
                                )
                            )
                            PrimaryButton(
                                text = stringResource(id = R.string.labor_mark_now),
                                onClick = viewModel::markLaborStartNow
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.labor_birth_display,
                                    state.summary.birthTime?.toReadableDateTime()
                                        ?: stringResource(id = R.string.unknown)
                                )
                            )
                            PrimaryButton(
                                text = stringResource(id = R.string.birth_time),
                                onClick = viewModel::markBirthNow
                            )
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.babyNameInput,
                                onValueChange = viewModel::updateBabyName,
                                label = { Text(text = stringResource(id = R.string.events_birth_name_hint)) }
                            )
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.weightInput,
                                onValueChange = viewModel::updateWeight,
                                label = { Text(text = stringResource(id = R.string.birth_weight)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.heightInput,
                                onValueChange = viewModel::updateHeight,
                                label = { Text(text = stringResource(id = R.string.birth_height)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            PrimaryButton(
                                text = stringResource(id = R.string.save_labor_info),
                                onClick = viewModel::saveSummary
                            )
                        }
                    }
                }
                item {
                    InfoCard(
                        title = stringResource(id = R.string.labor_event_title),
                        description = stringResource(id = R.string.labor_event_description),
                        icon = Icons.Outlined.Timeline
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
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
                            PrimaryButton(
                                text = stringResource(id = R.string.add_timeline_event),
                                onClick = viewModel::addCustomEvent
                            )
                        }
                    }
                }
                if (state.laborEvents.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.empty_state_title),
                            description = stringResource(id = R.string.no_timeline),
                            icon = Icons.Outlined.ChildCare
                        )
                    }
                } else {
                    items(state.laborEvents) { event ->
                        InfoCard(
                            title = event.title.ifBlank { stringResource(id = R.string.timeline_type_labor) },
                            description = event.description.ifBlank { stringResource(id = R.string.timeline_type_labor) },
                            icon = Icons.Outlined.ChildCare,
                            overline = event.timestamp.toReadableDateTime()
                        )
                    }
                }
            }
        }
    }
}
