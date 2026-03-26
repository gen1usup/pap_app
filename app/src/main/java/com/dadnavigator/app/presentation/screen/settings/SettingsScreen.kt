package com.dadnavigator.app.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Settings screen for personalization and maintenance operations.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    val infoMessage = state.infoRes?.let { stringResource(id = it) }
    val errorMessage = state.errorRes?.let { stringResource(id = it) }

    LaunchedEffect(infoMessage, errorMessage) {
        val message = errorMessage ?: infoMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    if (state.showResetDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissResetDialog,
            title = { Text(text = stringResource(id = R.string.settings_reset)) },
            text = { Text(text = stringResource(id = R.string.settings_reset_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissResetDialog()
                    viewModel.resetAllData()
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissResetDialog) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    ScreenScaffold(
        title = stringResource(id = R.string.settings_title),
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
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.fatherName,
                            onValueChange = viewModel::updateFatherName,
                            label = { Text(text = stringResource(id = R.string.settings_name)) }
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.dueDateInput,
                            onValueChange = viewModel::updateDueDate,
                            label = { Text(text = stringResource(id = R.string.settings_due_date)) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(id = R.string.settings_notifications),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = state.notificationsEnabled,
                                onCheckedChange = viewModel::updateNotifications
                            )
                        }
                    }
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(id = R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = state.themeMode == mode,
                                    onClick = { viewModel.updateThemeMode(mode) },
                                    label = { Text(text = stringResource(id = themeLabelRes(mode))) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::save
                ) {
                    Text(text = stringResource(id = R.string.settings_save))
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::askResetConfirmation
                ) {
                    Text(text = stringResource(id = R.string.settings_reset))
                }
            }

            item {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}

private fun themeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
