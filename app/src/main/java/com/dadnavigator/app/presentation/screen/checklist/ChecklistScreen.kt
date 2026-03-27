package com.dadnavigator.app.presentation.screen.checklist

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
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.Checklist
import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.presentation.component.ChecklistItem
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.TimelineActionButton

@Composable
fun ChecklistScreen(
    userId: String,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)? = null,
    onOpenTimeline: (() -> Unit)? = null,
    viewModel: ChecklistViewModel = hiltViewModel()
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
            viewModel.dismissMessages()
        }
    }

    ChecklistContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMenu = onMenu,
        onOpenTimeline = onOpenTimeline,
        onStageSelected = viewModel::selectStage,
        onNewChecklistTitleChanged = viewModel::updateNewChecklistTitle,
        onCreateChecklist = viewModel::createChecklist,
        onOpenRenameDialog = viewModel::openRenameDialog,
        onRenameDraftChanged = viewModel::updateRenameDraft,
        onConfirmRename = viewModel::renameChecklist,
        onDismissRename = viewModel::dismissRenameDialog,
        onRequestDeleteChecklist = viewModel::requestDeleteChecklist,
        onConfirmDeleteChecklist = viewModel::confirmDeleteChecklist,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onDraftChanged = viewModel::updateItemDraft,
        onAddItem = viewModel::addItem,
        onDeleteItem = viewModel::deleteItem,
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
    onOpenTimeline: (() -> Unit)?,
    onStageSelected: (AppStage) -> Unit,
    onNewChecklistTitleChanged: (String) -> Unit,
    onCreateChecklist: () -> Unit,
    onOpenRenameDialog: (Checklist) -> Unit,
    onRenameDraftChanged: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onDismissRename: () -> Unit,
    onRequestDeleteChecklist: (Checklist) -> Unit,
    onConfirmDeleteChecklist: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onDraftChanged: (Long, String) -> Unit,
    onAddItem: (Long) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onCheckedChanged: (Long, Boolean) -> Unit
) {
    val spacing = DadTheme.spacing
    val completed = state.checklists.sumOf { it.completedCount }
    val total = state.checklists.sumOf { it.totalCount }
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()

    state.renameTarget?.let { target ->
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = { Text(text = stringResource(id = R.string.checklist_rename_title)) },
            text = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.renameDraft,
                    onValueChange = onRenameDraftChanged,
                    label = { Text(text = stringResource(id = R.string.checklist_name_label)) }
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmRename) {
                    Text(text = stringResource(id = R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRename) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    state.deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text(text = stringResource(id = R.string.checklist_delete_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.checklist_delete_message,
                        target.title
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteChecklist) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    ScreenScaffold(
        title = stringResource(id = R.string.checklist_title),
        subtitle = stringResource(id = R.string.checklist_subtitle),
        onBack = onBack,
        onMenu = onMenu,
        snackbarHostState = snackbarHostState,
        actions = {
            if (onOpenTimeline != null) {
                TimelineActionButton(onClick = onOpenTimeline)
            }
        }
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
                            FlowRow(
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

                item {
                    InfoCard(
                        title = stringResource(id = R.string.checklist_create_title),
                        description = stringResource(
                            id = R.string.checklist_create_description,
                            stringResource(id = stageLabelRes(state.selectedStage))
                        ),
                        icon = Icons.Outlined.PlaylistAdd
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.newChecklistTitle,
                                onValueChange = onNewChecklistTitleChanged,
                                label = { Text(text = stringResource(id = R.string.checklist_name_label)) }
                            )
                            PrimaryButton(
                                text = stringResource(id = R.string.checklist_create_action),
                                onClick = onCreateChecklist
                            )
                        }
                    }
                }

                if (state.checklists.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.empty_state_title),
                            description = stringResource(id = R.string.checklist_empty_stage),
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
                                draft = state.itemDrafts[checklistWithItems.checklist.id].orEmpty(),
                                onDraftChanged = { onDraftChanged(checklistWithItems.checklist.id, it) },
                                onAddItem = { onAddItem(checklistWithItems.checklist.id) },
                                onDeleteItem = onDeleteItem,
                                onCheckedChanged = onCheckedChanged,
                                onRenameChecklist = {
                                    onOpenRenameDialog(checklistWithItems.checklist)
                                },
                                onDeleteChecklist = {
                                    onRequestDeleteChecklist(checklistWithItems.checklist)
                                }
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
    onDeleteItem: (Long) -> Unit,
    onCheckedChanged: (Long, Boolean) -> Unit,
    onRenameChecklist: () -> Unit,
    onDeleteChecklist: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = checklist.checklist.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (checklist.checklist.isSystem) {
                            stringResource(id = R.string.checklist_system_label)
                        } else {
                            stringResource(id = R.string.checklist_custom_label)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!checklist.checklist.isSystem) {
                    TextButton(onClick = onRenameChecklist) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null
                        )
                        Text(text = stringResource(id = R.string.action_edit))
                    }
                    TextButton(onClick = onDeleteChecklist) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null
                        )
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                }
            }
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
                    onCheckedChange = { checked -> onCheckedChanged(item.id, checked) },
                    onDelete = { onDeleteItem(item.id) }
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft,
                onValueChange = onDraftChanged,
                label = { Text(text = stringResource(id = R.string.custom_item_hint)) }
            )
            SecondaryButton(
                text = stringResource(id = R.string.add_item),
                onClick = onAddItem
            )
        }
    }
}

private fun stageLabelRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.app_stage_preparing
    AppStage.CONTRACTIONS -> R.string.app_stage_contractions
    AppStage.AT_HOSPITAL -> R.string.app_stage_at_hospital
    AppStage.AT_HOME -> R.string.app_stage_at_home
}
