package com.dadnavigator.app.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.presentation.component.DangerButton
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@OptIn(ExperimentalLayoutApi::class)
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

    val spacing = DadTheme.spacing
    ScreenScaffold(
        title = stringResource(id = R.string.settings_title),
        subtitle = stringResource(id = R.string.settings_subtitle),
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
                        title = stringResource(id = R.string.settings_profile_title),
                        description = stringResource(id = R.string.settings_profile_description),
                        icon = Icons.Outlined.Person
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
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
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(id = R.string.settings_notifications),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
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
                    InfoCard(
                        title = stringResource(id = R.string.settings_theme),
                        description = stringResource(id = R.string.settings_theme_description),
                        icon = Icons.Outlined.DarkMode
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
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
                item {
                    PrimaryButton(
                        text = stringResource(id = R.string.settings_save),
                        onClick = viewModel::save
                    )
                }
                item {
                    DangerButton(
                        text = stringResource(id = R.string.settings_reset),
                        onClick = viewModel::askResetConfirmation,
                        icon = Icons.Outlined.RestartAlt
                    )
                }
            }
        }
    }
}

private fun themeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
