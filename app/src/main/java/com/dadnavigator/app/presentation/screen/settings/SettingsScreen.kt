package com.dadnavigator.app.presentation.screen.settings

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ThemeMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
                TextButton(
                    onClick = {
                        viewModel.dismissResetDialog()
                        viewModel.resetAllData()
                    }
                ) {
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
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT)
    }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        containerColor = colors.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.settings_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(id = R.string.settings_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.nav_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            colors.surface,
                            colors.surfaceContainerLow.copy(alpha = 0.82f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("settings_list"),
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    SettingsSectionCard(
                        title = stringResource(id = R.string.settings_profile_title),
                        description = stringResource(id = R.string.settings_profile_description),
                        icon = Icons.Outlined.Person
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
                            label = { Text(text = stringResource(id = R.string.settings_due_date)) },
                            isError = state.dueDateErrorRes != null,
                            supportingText = {
                                Text(
                                    text = stringResource(
                                        id = state.dueDateErrorRes ?: R.string.settings_due_date_hint
                                    )
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val initialDate = runCatching {
                                            LocalDate.parse(state.dueDateInput, dateFormatter)
                                        }.getOrElse { LocalDate.now() }
                                            DatePickerDialog(
                                                context,
                                                { _, year: Int, month: Int, dayOfMonth: Int ->
                                                    viewModel.updateDueDate(
                                                        LocalDate.of(year, month + 1, dayOfMonth).format(dateFormatter)
                                                    )
                                                },
                                            initialDate.year,
                                            initialDate.monthValue - 1,
                                            initialDate.dayOfMonth
                                        ).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Event,
                                        contentDescription = stringResource(id = R.string.settings_pick_due_date)
                                    )
                                }
                            }
                        )
                        androidx.compose.foundation.layout.Row(
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

                item {
                    SettingsSectionCard(
                        title = stringResource(id = R.string.settings_stage_title),
                        description = stringResource(id = R.string.settings_stage_description),
                        icon = Icons.Outlined.Route
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            AppStage.entries.forEach { stage: AppStage ->
                                FilterChip(
                                    selected = state.appStage == stage,
                                    onClick = { viewModel.updateAppStage(stage) },
                                    label = { Text(text = stringResource(id = appStageLabelRes(stage))) }
                                )
                            }
                        }
                    }
                }

                item {
                    SettingsSectionCard(
                        title = stringResource(id = R.string.settings_theme),
                        description = stringResource(id = R.string.settings_theme_description),
                        icon = Icons.Outlined.DarkMode
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            ThemeMode.entries.forEach { mode: ThemeMode ->
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
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::save,
                        shape = DadTheme.shapes.pill,
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Text(text = stringResource(id = R.string.settings_save))
                    }
                }

                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::askResetConfirmation,
                        shape = DadTheme.shapes.pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error,
                            contentColor = colors.onError
                        ),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = null
                        )
                        Text(text = stringResource(id = R.string.settings_reset))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(DadTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md),
            content = {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xs)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                content()
            }
        )
    }
}

private fun themeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

private fun appStageLabelRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.LABOR -> R.string.app_stage_labor
    AppStage.AFTER_BIRTH -> R.string.app_stage_after_birth
}
