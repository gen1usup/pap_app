package com.dadnavigator.app.presentation.screen.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NightlightRound
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.TimelineItem
import java.time.Instant

@Composable
fun TimelineScreen(
    userId: String,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)? = null,
    viewModel: TimelineViewModel = hiltViewModel()
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

    TimelineContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onMenu = onMenu,
        onFilterSelected = viewModel::setFilter,
        onToggleExpanded = viewModel::toggleExpanded,
        onShowAddSheet = viewModel::showAddSheet,
        onHideAddSheet = viewModel::hideAddSheet,
        onTypeSelected = viewModel::updateType,
        onTitleChanged = viewModel::updateTitle,
        onDescriptionChanged = viewModel::updateDescription,
        onSave = viewModel::saveEvent
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TimelineContent(
    state: TimelineUiState,
    snackbarHostState: SnackbarHostState,
    onBack: (() -> Unit)?,
    onMenu: (() -> Unit)?,
    onFilterSelected: (TimelineFilter) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onShowAddSheet: () -> Unit,
    onHideAddSheet: () -> Unit,
    onTypeSelected: (TimelineType) -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val spacing = DadTheme.spacing

    ScreenScaffold(
        title = stringResource(id = R.string.timeline_title),
        subtitle = stringResource(id = R.string.timeline_subtitle),
        onBack = onBack,
        onMenu = onMenu,
        snackbarHostState = snackbarHostState,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddSheet,
                text = { Text(text = stringResource(id = R.string.timeline_add_event)) },
                icon = { androidx.compose.material3.Icon(Icons.Outlined.Add, contentDescription = null) }
            )
        }
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    horizontal = spacing.md,
                    vertical = spacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        TimelineFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = state.filter == filter,
                                onClick = { onFilterSelected(filter) },
                                label = {
                                    Text(
                                        text = stringResource(
                                            id = when (filter) {
                                                TimelineFilter.ALL -> R.string.timeline_filter_all
                                                TimelineFilter.LABOR -> R.string.timeline_filter_labor
                                                TimelineFilter.POSTPARTUM -> R.string.timeline_filter_postpartum
                                            }
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                if (state.events.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.timeline_empty_title),
                            description = stringResource(id = R.string.timeline_empty_description),
                            icon = Icons.Outlined.Timeline,
                            action = {
                                SecondaryButton(
                                    text = stringResource(id = R.string.timeline_add_event),
                                    onClick = onShowAddSheet,
                                    fullWidth = false
                                )
                            }
                        )
                    }
                } else {
                    items(state.events) { event ->
                        val canExpand = event.description.isNotBlank()
                        TimelineItem(
                            title = event.title.ifBlank { stringResource(id = timelineTypeLabel(event.type)) },
                            subtitle = stringResource(id = timelineTypeLabel(event.type)),
                            time = event.timestamp.toReadableDateTime().takeLast(5),
                            description = event.description.takeIf { it.isNotBlank() },
                            icon = timelineTypeIcon(event.type),
                            expanded = event.id in state.expandedEventIds,
                            onToggleExpanded = if (canExpand) {
                                { onToggleExpanded(event.id) }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }

    if (state.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = onHideAddSheet,
            shape = DadTheme.shapes.sheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    text = stringResource(id = R.string.timeline_add_event),
                    style = MaterialTheme.typography.headlineSmall
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    listOf(
                        TimelineType.LABOR,
                        TimelineType.BIRTH,
                        TimelineType.NOTE,
                        TimelineType.WATER_BREAK,
                        TimelineType.FEEDING
                    ).forEach { type ->
                        FilterChip(
                            selected = state.selectedType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(text = stringResource(id = timelineTypeLabel(type))) }
                        )
                    }
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.draftTitle,
                    onValueChange = onTitleChanged,
                    label = { Text(text = stringResource(id = R.string.timeline_title_field)) }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.draftDescription,
                    onValueChange = onDescriptionChanged,
                    minLines = 3,
                    label = { Text(text = stringResource(id = R.string.timeline_description_field)) }
                )
                PrimaryButton(
                    text = stringResource(id = R.string.save_event),
                    onClick = {
                        onSave()
                        onHideAddSheet()
                    }
                )
            }
        }
    }
}

private fun timelineTypeLabel(type: TimelineType): Int = when (type) {
    TimelineType.CONTRACTION -> R.string.timeline_type_contraction
    TimelineType.WATER_BREAK -> R.string.timeline_type_water_break
    TimelineType.LABOR -> R.string.timeline_type_labor
    TimelineType.BIRTH -> R.string.timeline_type_birth
    TimelineType.PREPARATION_NOTE -> R.string.timeline_type_preparation_note
    TimelineType.LABOR_NOTE -> R.string.timeline_type_labor_note
    TimelineType.HOSPITAL_NOTE -> R.string.timeline_type_hospital_note
    TimelineType.HOME_NOTE -> R.string.timeline_type_home_note
    TimelineType.FEEDING -> R.string.timeline_type_feeding
    TimelineType.DIAPER -> R.string.timeline_type_diaper
    TimelineType.SLEEP -> R.string.timeline_type_sleep
    TimelineType.NOTE -> R.string.timeline_type_note
}

private fun timelineTypeIcon(type: TimelineType): ImageVector = when (type) {
    TimelineType.CONTRACTION -> Icons.Outlined.MonitorHeart
    TimelineType.WATER_BREAK -> Icons.Outlined.WaterDrop
    TimelineType.LABOR -> Icons.Outlined.LocalHospital
    TimelineType.BIRTH -> Icons.Outlined.ChildCare
    TimelineType.PREPARATION_NOTE,
    TimelineType.LABOR_NOTE,
    TimelineType.HOSPITAL_NOTE,
    TimelineType.HOME_NOTE -> Icons.Outlined.EditNote
    TimelineType.FEEDING -> Icons.Outlined.FavoriteBorder
    TimelineType.DIAPER -> Icons.Outlined.BabyChangingStation
    TimelineType.SLEEP -> Icons.Outlined.NightlightRound
    TimelineType.NOTE -> Icons.Outlined.EditNote
}

@Preview(showBackground = true)
@Composable
private fun TimelinePreview() {
    DadNavigatorTheme(dynamicColor = false) {
        TimelineContent(
            state = TimelineUiState(
                events = listOf(
                    TimelineEvent(1, "u", TimelineType.LABOR, Instant.now(), "Приехали в роддом", "Оформление прошло быстро"),
                    TimelineEvent(2, "u", TimelineType.NOTE, Instant.now(), "", "Собрали документы и воду")
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onMenu = {},
            onFilterSelected = {},
            onToggleExpanded = {},
            onShowAddSheet = {},
            onHideAddSheet = {},
            onTypeSelected = {},
            onTitleChanged = {},
            onDescriptionChanged = {},
            onSave = {}
        )
    }
}
