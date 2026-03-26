package com.dadnavigator.app.presentation.screen.checklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
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
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Checklist management screen.
 */
@Composable
fun ChecklistScreen(
    userId: String,
    onBack: () -> Unit,
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

    ScreenScaffold(
        title = stringResource(id = R.string.checklist_title),
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.checklists.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(id = R.string.empty_state_title),
                        description = stringResource(id = R.string.no_items)
                    )
                }
            } else {
                items(state.checklists) { checklistWithItems ->
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = checklistWithItems.checklist.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(
                                    id = R.string.checklist_progress,
                                    checklistWithItems.completedCount,
                                    checklistWithItems.totalCount
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            checklistWithItems.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { checked ->
                                            viewModel.setItemChecked(item.id, checked)
                                        }
                                    )
                                    Text(
                                        text = item.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = state.drafts[checklistWithItems.checklist.id].orEmpty(),
                                onValueChange = { text ->
                                    viewModel.updateDraft(checklistWithItems.checklist.id, text)
                                },
                                label = { Text(text = stringResource(id = R.string.custom_item_hint)) }
                            )
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { viewModel.addItem(checklistWithItems.checklist.id) }
                            ) {
                                Text(text = stringResource(id = R.string.add_item))
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
