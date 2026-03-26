package com.dadnavigator.app.presentation.screen.contraction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.core.util.toReadableDuration
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.ScreenScaffold
import java.time.Duration

/**
 * Contraction counter screen.
 */
@Composable
fun ContractionScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: ContractionViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.errorRes?.let { stringResource(id = it) }
    var showFinishSessionConfirmation by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(message = errorMessage)
            viewModel.dismissError()
        }
    }

    ScreenScaffold(
        title = stringResource(id = R.string.contraction_title),
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (state.isSessionActive) {
                                stringResource(id = R.string.session_status_active)
                            } else {
                                stringResource(id = R.string.session_status_idle)
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        StatRow(
                            label = stringResource(id = R.string.session_duration),
                            value = state.sessionDuration.toReadableDuration()
                        )
                        StatRow(
                            label = stringResource(id = R.string.current_duration),
                            value = state.currentContractionDuration.toReadableDuration()
                        )
                        Text(
                            text = stringResource(id = R.string.stat_count, state.stats.count),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (state.isSessionActive) {
                            showFinishSessionConfirmation = true
                        } else {
                            viewModel.startSession()
                        }
                    }
                ) {
                    Text(
                        text = if (state.isSessionActive) {
                            stringResource(id = R.string.finish_session)
                        } else {
                            stringResource(id = R.string.start_session)
                        }
                    )
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isSessionActive,
                    onClick = viewModel::startOrFinishContraction
                ) {
                    Text(
                        text = if (state.activeContractionId == null) {
                            stringResource(id = R.string.start_contraction)
                        } else {
                            stringResource(id = R.string.stop_contraction)
                        }
                    )
                }
            }

            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(id = R.string.recommendation), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(id = recommendationTextRes(state.stats.recommendationLevel)),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        StatRow(
                            label = stringResource(id = R.string.average_duration),
                            value = state.stats.averageDuration?.toReadableDuration() ?: stringResource(id = R.string.unknown)
                        )
                        StatRow(
                            label = stringResource(id = R.string.average_interval),
                            value = state.stats.averageInterval?.toReadableDuration() ?: stringResource(id = R.string.unknown)
                        )
                        StatRow(
                            label = stringResource(id = R.string.recent_contraction_count),
                            value = state.stats.recentContractionCount.toString()
                        )
                        StatRow(
                            label = stringResource(id = R.string.recent_interval_count),
                            value = state.stats.recentIntervalCount.toString()
                        )
                        StatRow(
                            label = stringResource(id = R.string.recent_average_duration),
                            value = state.stats.recentAverageDuration?.toReadableDuration() ?: stringResource(id = R.string.unknown)
                        )
                        StatRow(
                            label = stringResource(id = R.string.recent_average_interval),
                            value = state.stats.recentAverageInterval?.toReadableDuration() ?: stringResource(id = R.string.unknown)
                        )
                        StatRow(
                            label = stringResource(id = R.string.last_interval),
                            value = state.stats.lastInterval?.toReadableDuration() ?: stringResource(id = R.string.unknown)
                        )
                        StatRow(
                            label = stringResource(id = R.string.interval_std_deviation),
                            value = state.stats.intervalStdDeviation?.toReadableDuration() ?: stringResource(id = R.string.unknown)
                        )
                        StatRow(
                            label = stringResource(id = R.string.recent_window_span),
                            value = state.stats.recentWindowSpan.toReadableDuration()
                        )
                        StatRow(
                            label = stringResource(id = R.string.current_pattern_held_for),
                            value = state.stats.currentPatternHeldFor.toReadableDuration()
                        )
                        StatRow(
                            label = stringResource(id = R.string.pattern_regular_prepare),
                            value = stringResource(id = regularityTextRes(state.stats.isRegularForPrepare))
                        )
                        StatRow(
                            label = stringResource(id = R.string.pattern_regular_go),
                            value = stringResource(id = regularityTextRes(state.stats.isRegularForGo))
                        )
                        StatRow(
                            label = stringResource(id = R.string.trend),
                            value = stringResource(id = trendTextRes(state.stats.trend))
                        )
                    }
                }
            }

            item {
                Text(text = stringResource(id = R.string.history), style = MaterialTheme.typography.titleMedium)
            }

            val completed = viewModel.completedContractions()
            if (completed.isEmpty()) {
                item {
                    EmptyState(
                        title = stringResource(id = R.string.empty_state_title),
                        description = stringResource(id = R.string.no_history)
                    )
                }
            } else {
                itemsIndexed(completed.reversed()) { index, contraction ->
                    val originalIndex = completed.size - index
                    val next = completed.getOrNull(originalIndex - 2)
                    val interval = next?.let { Duration.between(it.startedAt, contraction.startedAt) }

                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.contraction_item_title, originalIndex),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = contraction.startedAt.toReadableDateTime(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stringResource(id = R.string.current_duration)}: ${contraction.duration?.toReadableDuration() ?: stringResource(id = R.string.unknown)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${stringResource(id = R.string.last_interval)}: ${interval?.toReadableDuration() ?: stringResource(id = R.string.unknown)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            item {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }

    if (showFinishSessionConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishSessionConfirmation = false },
            title = {
                Text(text = stringResource(id = R.string.finish_session_confirm_title))
            },
            text = {
                Text(text = stringResource(id = R.string.finish_session_confirm_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishSessionConfirmation = false
                        viewModel.finishSession()
                    }
                ) {
                    Text(text = stringResource(id = R.string.finish_session))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishSessionConfirmation = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyLarge
    )
}

private fun recommendationTextRes(level: RecommendationLevel): Int = when (level) {
    RecommendationLevel.MONITOR -> R.string.recommendation_monitor
    RecommendationLevel.PREPARE -> R.string.recommendation_prepare
    RecommendationLevel.GO_TO_HOSPITAL -> R.string.recommendation_go_hospital
    RecommendationLevel.EMERGENCY -> R.string.recommendation_emergency
}

private fun regularityTextRes(isRegular: Boolean): Int = if (isRegular) {
    R.string.pattern_regular_yes
} else {
    R.string.pattern_regular_no
}

private fun trendTextRes(trend: ContractionTrend): Int = when (trend) {
    ContractionTrend.STABLE -> R.string.trend_stable
    ContractionTrend.BECOMING_MORE_INTENSE -> R.string.trend_more_intense
    ContractionTrend.BECOMING_WEAKER -> R.string.trend_weaker
    ContractionTrend.INSUFFICIENT_DATA -> R.string.trend_insufficient
}
