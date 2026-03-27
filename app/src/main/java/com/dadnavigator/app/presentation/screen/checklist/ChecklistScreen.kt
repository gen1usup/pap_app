package com.dadnavigator.app.presentation.screen.checklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.presentation.component.ChecklistItem
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@Composable
fun ChecklistScreen(
    userId: String,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)? = null,
    viewModel: ChecklistViewModel = hiltViewModel()
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

    ChecklistContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMenu = onMenu,
        onStageSelected = viewModel::selectStage,
        onDraftChanged = viewModel::updateDraft,
        onAddItem = viewModel::addItem,
        onCheckedChanged = viewModel::setItemChecked
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChecklistContent(
    state: ChecklistUiState,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)?,
    onStageSelected: (AppStage) -> Unit,
    onDraftChanged: (Long, String) -> Unit,
    onAddItem: (Long) -> Unit,
    onCheckedChanged: (Long, Boolean) -> Unit
) {
    val spacing = DadTheme.spacing
    val completed = state.checklists.sumOf { it.completedCount }
    val total = state.checklists.sumOf { it.totalCount }
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()

    ScreenScaffold(
        title = stringResource(id = R.string.checklist_title),
        subtitle = stringResource(id = R.string.checklist_subtitle),
        onBack = onBack,
        onMenu = onMenu,
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
                        title = stringResource(id = stageLabelRes(state.selectedStage)),
                        description = stringResource(
                            id = R.string.checklist_progress,
                            completed,
                            total
                        ),
                        icon = Icons.Outlined.Checklist,
                        overline = stringResource(id = R.string.checklist_summary_overline)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                            androidx.compose.foundation.layout.FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                                verticalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                AppStage.entries.forEach { stage ->
                                    FilterChip(
                                        selected = state.selectedStage == stage,
                                        onClick = { onStageSelected(stage) },
                                        label = { Text(text = stringResource(id = stageLabelRes(stage))) }
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                progress = { progress }
                            )
                        }
                    }
                }

                if (state.checklists.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.empty_state_title),
                            description = stringResource(id = R.string.no_items),
                            icon = Icons.Outlined.AddTask
                        )
                    }
                } else {
                    val grouped = state.checklists.groupBy { it.checklist.category }
                    grouped.forEach { (category, checklists) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        items(checklists, key = { it.checklist.id }) { checklistWithItems ->
                            ChecklistGroupCard(
                                checklist = checklistWithItems,
                                draft = state.drafts[checklistWithItems.checklist.id].orEmpty(),
                                onDraftChanged = { onDraftChanged(checklistWithItems.checklist.id, it) },
                                onAddItem = { onAddItem(checklistWithItems.checklist.id) },
                                onCheckedChanged = onCheckedChanged
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistGroupCard(
    checklist: ChecklistWithItems,
    draft: String,
    onDraftChanged: (String) -> Unit,
    onAddItem: () -> Unit,
    onCheckedChanged: (Long, Boolean) -> Unit
) {
    val spacing = DadTheme.spacing
    val progress = if (checklist.totalCount == 0) 0f else {
        checklist.completedCount.toFloat() / checklist.totalCount.toFloat()
    }

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
                text = checklist.checklist.title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(
                    id = R.string.checklist_progress,
                    checklist.completedCount,
                    checklist.totalCount
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress }
            )
            checklist.items.forEach { item ->
                ChecklistItem(
                    title = item.text,
                    checked = item.isChecked,
                    onCheckedChange = { checked -> onCheckedChanged(item.id, checked) }
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft,
                onValueChange = onDraftChanged,
                label = { Text(text = stringResource(id = R.string.custom_item_hint)) }
            )
            PrimaryButton(
                text = stringResource(id = R.string.add_item),
                onClick = onAddItem
            )
        }
    }
}

private fun stageLabelRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.LABOR -> R.string.app_stage_labor
    AppStage.AFTER_BIRTH -> R.string.app_stage_after_birth
}
