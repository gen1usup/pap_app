package com.dadnavigator.app.presentation.screen.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Unified chronology screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ScreenScaffold(
        title = stringResource(id = R.string.timeline_title),
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.filter == TimelineFilter.ALL,
                        onClick = { viewModel.setFilter(TimelineFilter.ALL) },
                        label = { Text(text = stringResource(id = R.string.timeline_filter_all)) }
                    )
                    FilterChip(
                        selected = state.filter == TimelineFilter.LABOR,
                        onClick = { viewModel.setFilter(TimelineFilter.LABOR) },
                        label = { Text(text = stringResource(id = R.string.timeline_filter_labor)) }
                    )
                    FilterChip(
                        selected = state.filter == TimelineFilter.POSTPARTUM,
                        onClick = { viewModel.setFilter(TimelineFilter.POSTPARTUM) },
                        label = { Text(text = stringResource(id = R.string.timeline_filter_postpartum)) }
                    )
                }
            }

            if (state.events.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(id = R.string.empty_state_title),
                        description = stringResource(id = R.string.no_timeline)
                    )
                }
            } else {
                items(state.events) { event ->
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = event.title.ifBlank { stringResource(id = timelineTypeLabel(event.type)) },
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
        }
    }
}

private fun timelineTypeLabel(type: TimelineType): Int = when (type) {
    TimelineType.CONTRACTION -> R.string.timeline_type_contraction
    TimelineType.WATER_BREAK -> R.string.timeline_type_water_break
    TimelineType.LABOR -> R.string.timeline_type_labor
    TimelineType.BIRTH -> R.string.timeline_type_birth
    TimelineType.FEEDING -> R.string.timeline_type_feeding
    TimelineType.DIAPER -> R.string.timeline_type_diaper
    TimelineType.SLEEP -> R.string.timeline_type_sleep
    TimelineType.NOTE -> R.string.timeline_type_note
}
