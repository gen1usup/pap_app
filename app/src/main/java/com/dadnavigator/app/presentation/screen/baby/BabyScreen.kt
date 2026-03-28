package com.dadnavigator.app.presentation.screen.baby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material3.MaterialTheme
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
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@Composable
fun BabyScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: BabyViewModel = hiltViewModel()
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
        title = stringResource(id = R.string.baby_title),
        subtitle = stringResource(id = R.string.baby_subtitle),
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
                        title = state.summary.babyName?.takeIf { it.isNotBlank() }
                            ?: stringResource(id = R.string.baby_card_title_empty),
                        description = state.summary.birthTime?.toReadableDateTime()
                            ?: stringResource(id = R.string.baby_birth_unknown),
                        icon = Icons.Outlined.ChildCare,
                        overline = stringResource(id = R.string.baby_card_overline)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            Text(
                                text = stringResource(
                                    id = R.string.baby_metrics_value,
                                    state.summary.birthWeightGrams?.toString() ?: stringResource(id = R.string.unknown),
                                    state.summary.birthHeightCm?.toString() ?: stringResource(id = R.string.unknown)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item {
                    InfoCard(
                        title = stringResource(id = R.string.baby_edit_title),
                        description = stringResource(id = R.string.baby_edit_description),
                        icon = Icons.Outlined.ChildCare
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.nameInput,
                                onValueChange = viewModel::updateName,
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
                                text = stringResource(id = R.string.baby_save),
                                onClick = viewModel::save
                            )
                        }
                    }
                }
            }
        }
    }
}
