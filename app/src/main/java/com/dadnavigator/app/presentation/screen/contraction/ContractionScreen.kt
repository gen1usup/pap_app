package com.dadnavigator.app.presentation.screen.contraction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.core.util.toReadableDateTime
import com.dadnavigator.app.core.util.toReadableDuration
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.service.ActiveLaborRecommendation
import com.dadnavigator.app.domain.service.ActiveLaborRecommendationSource
import com.dadnavigator.app.presentation.component.EmptyState
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone
import com.dadnavigator.app.presentation.component.recommendationHeadlineRes as contractionRecommendationHeadlineRes
import com.dadnavigator.app.presentation.component.recommendationTextRes as contractionRecommendationTextRes
import com.dadnavigator.app.presentation.component.recommendationTone as contractionRecommendationTone
import java.time.Duration
import java.time.Instant

@Composable
fun ContractionScreen(
    userId: String,
    onBack: () -> Unit,
    onOpenWaterBreak: () -> Unit,
    viewModel: ContractionViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val message = state.errorRes?.let { stringResource(id = it) }
        ?: state.infoRes?.let { stringResource(id = it) }
    val haptics = LocalHapticFeedback.current
    val birthTitle = stringResource(id = R.string.events_action_birth)
    var showFinishSessionConfirmation by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteContractionId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message = message)
            viewModel.dismissError()
        }
    }

    ContractionContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onPrimaryAction = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.startOrFinishContraction()
        },
        onAskFinishSession = { showFinishSessionConfirmation = true },
        onMarkBirth = {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            viewModel.markBirthNow(eventTitle = birthTitle)
        },
        onOpenWaterBreak = onOpenWaterBreak,
        onAskDeleteContraction = { pendingDeleteContractionId = it }
    )

    if (pendingDeleteContractionId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteContractionId = null },
            title = { Text(text = stringResource(id = R.string.delete_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.contraction_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val contractionId = pendingDeleteContractionId ?: return@TextButton
                        pendingDeleteContractionId = null
                        viewModel.deleteContraction(contractionId)
                    }
                ) {
                    Text(text = stringResource(id = R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteContractionId = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if (showFinishSessionConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinishSessionConfirmation = false },
            title = { Text(text = stringResource(id = R.string.finish_session_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.finish_session_confirm_message)) },
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
private fun ContractionContent(
    state: ContractionUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
    onAskFinishSession: () -> Unit,
    onMarkBirth: () -> Unit,
    onOpenWaterBreak: () -> Unit,
    onAskDeleteContraction: (Long) -> Unit
) {
    val spacing = DadTheme.spacing
    val completedContractions = state.contractions.filter { it.endedAt != null }.reversed()

    ScreenScaffold(
        title = stringResource(id = R.string.contraction_title),
        subtitle = stringResource(id = R.string.contraction_subtitle),
        onBack = onBack,
        snackbarHostState = snackbarHostState
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
                    StatusCard(
                        title = stringResource(id = activeLaborHeadlineRes(state.recommendation)),
                        description = stringResource(id = activeLaborTextRes(state.recommendation)),
                        tone = activeLaborTone(state.recommendation),
                        headline = stringResource(id = R.string.active_labor_recommendation_overline)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(id = R.string.dashboard_live_contraction_status_label),
                            value = if (state.activeContractionId != null) {
                                state.currentContractionDuration.toReadableDuration()
                            } else {
                                stringResource(id = R.string.status_inactive)
                            }
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(id = R.string.dashboard_live_contraction_interval_label),
                            value = state.currentInterval?.toReadableDuration()
                                ?: stringResource(id = R.string.unknown)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(id = R.string.session_duration),
                            value = state.sessionDuration.toReadableDuration()
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(id = R.string.stat_count_short),
                            value = state.stats.count.toString()
                        )
                    }
                }

                item {
                    WaterStatusCard(
                        latestWaterBreak = state.latestWaterBreak,
                        waterBreakElapsed = state.waterBreakElapsed,
                        onOpenWaterBreak = onOpenWaterBreak
                    )
                }

                item {
                    BigContractionButton(
                        isSessionActive = state.isSessionActive,
                        isContractionRunning = state.activeContractionId != null,
                        currentDuration = state.currentContractionDuration.toReadableDuration(),
                        onPrimaryAction = onPrimaryAction,
                        onFinishSession = onAskFinishSession
                    )
                }

                item {
                    PrimaryBirthButton(
                        onClick = onMarkBirth
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(id = R.string.average_interval),
                            value = state.stats.averageInterval?.toReadableDuration()
                                ?: stringResource(id = R.string.unknown)
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(id = R.string.average_duration),
                            value = state.stats.averageDuration?.toReadableDuration()
                                ?: stringResource(id = R.string.unknown)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = stringResource(id = R.string.contraction_graph_title),
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (completedContractions.isEmpty()) {
                                EmptyState(
                                    title = stringResource(id = R.string.empty_state_title),
                                    description = stringResource(id = R.string.no_history)
                                )
                            } else {
                                ContractionMiniGraph(contractions = completedContractions.take(6))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(id = R.string.history),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (completedContractions.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(id = R.string.empty_state_title),
                            description = stringResource(id = R.string.no_history)
                        )
                    }
                } else {
                    itemsIndexed(completedContractions) { index, contraction ->
                        val next = completedContractions.getOrNull(index + 1)
                        val interval = next?.let { Duration.between(it.startedAt, contraction.startedAt) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = DadTheme.shapes.card,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(spacing.xs)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.contraction_item_title, completedContractions.size - index),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    IconButton(onClick = { onAskDeleteContraction(contraction.id) }) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = stringResource(id = R.string.action_delete)
                                        )
                                    }
                                }
                                Text(
                                    text = contraction.startedAt.toReadableDateTime(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(
                                        id = R.string.contraction_history_duration,
                                        contraction.duration?.toReadableDuration()
                                            ?: stringResource(id = R.string.unknown)
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(
                                        id = R.string.contraction_history_interval,
                                        interval?.toReadableDuration()
                                            ?: stringResource(id = R.string.unknown)
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterStatusCard(
    latestWaterBreak: WaterBreakEvent?,
    waterBreakElapsed: Duration?,
    onOpenWaterBreak: () -> Unit
) {
    InfoCard(
        title = stringResource(id = R.string.active_labor_water_title),
        description = waterStatusText(latestWaterBreak),
        icon = Icons.Outlined.WaterDrop,
        overline = stringResource(id = R.string.water_break_overline)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
            if (waterBreakElapsed != null) {
                Text(
                    text = stringResource(
                        id = R.string.active_labor_water_elapsed,
                        waterBreakElapsed.toReadableDuration()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SecondaryButton(
                text = stringResource(
                    id = if (latestWaterBreak == null) {
                        R.string.active_labor_record_water_break
                    } else {
                        R.string.action_water_break_timer
                    }
                ),
                onClick = onOpenWaterBreak,
                icon = Icons.Outlined.WaterDrop
            )
        }
    }
}

@Composable
private fun waterStatusText(event: WaterBreakEvent?): String {
    if (event == null) {
        return stringResource(id = R.string.active_labor_water_not_broken)
    }
    return stringResource(
        id = R.string.active_labor_water_broke_at,
        stringResource(id = waterColorLabelRes(event.color)),
        event.happenedAt.toReadableDateTime()
    )
}

@Composable
private fun BigContractionButton(
    isSessionActive: Boolean,
    isContractionRunning: Boolean,
    currentDuration: String,
    onPrimaryAction: () -> Unit,
    onFinishSession: () -> Unit
) {
    val spacing = DadTheme.spacing
    val headline = when {
        !isSessionActive -> stringResource(id = R.string.contraction_ready_title)
        isContractionRunning -> stringResource(id = R.string.contraction_active_label)
        else -> stringResource(id = R.string.contraction_waiting_label)
    }
    val helperText = when {
        !isSessionActive -> stringResource(id = R.string.contraction_ready_description)
        isContractionRunning -> stringResource(id = R.string.contraction_primary_hint_running)
        else -> stringResource(id = R.string.contraction_primary_hint_idle)
    }
    val circleLabel = when {
        !isSessionActive -> stringResource(id = R.string.contraction_circle_start_first)
        isContractionRunning -> stringResource(id = R.string.stop_contraction)
        else -> stringResource(id = R.string.start_contraction)
    }
    val circleColor by animateColorAsState(
        targetValue = when {
            isContractionRunning -> MaterialTheme.colorScheme.errorContainer
            isSessionActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.secondaryContainer
        },
        label = "contraction_circle_color"
    )
    val circleContentColor by animateColorAsState(
        targetValue = when {
            isContractionRunning -> MaterialTheme.colorScheme.onErrorContainer
            isSessionActive -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSecondaryContainer
        },
        label = "contraction_circle_content_color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Surface(
                modifier = Modifier
                    .size(236.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = circleLabel
                    },
                shape = CircleShape,
                color = circleColor,
                contentColor = circleContentColor,
                shadowElevation = 2.dp,
                onClick = onPrimaryAction
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = circleLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = spacing.lg)
                    )
                    if (isContractionRunning) {
                        Text(
                            text = currentDuration,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (isSessionActive) {
                SecondaryButton(
                    text = stringResource(id = R.string.finish_session),
                    onClick = onFinishSession
                )
            }
        }
    }
}

@Composable
private fun PrimaryBirthButton(
    onClick: () -> Unit
) {
    SecondaryButton(
        text = stringResource(id = R.string.events_action_birth),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        fullWidth = true
    )
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = DadTheme.shapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(DadTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.xxs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ContractionMiniGraph(contractions: List<Contraction>) {
    val spacing = DadTheme.spacing
    val values = contractions.mapNotNull { it.duration?.seconds?.toFloat() }
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.Bottom
    ) {
        contractions.forEach { contraction ->
            val value = contraction.duration?.seconds?.toFloat() ?: 0f
            val fraction = (value / max).coerceIn(0.15f, 1f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height((92 * fraction).dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = DadTheme.shapes.pill
                        )
                )
                Text(
                    text = "${value.toInt()}с",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun activeLaborHeadlineRes(recommendation: ActiveLaborRecommendation): Int = when (recommendation.source) {
    ActiveLaborRecommendationSource.CONTRACTIONS -> contractionRecommendationHeadlineRes(recommendation.level)
    ActiveLaborRecommendationSource.WATER_BREAK_CLEAR -> R.string.active_labor_recommendation_water_title
    ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR -> R.string.active_labor_recommendation_urgent_title
    ActiveLaborRecommendationSource.BIRTH_RECORDED -> R.string.active_labor_recommendation_birth_title
}

private fun activeLaborTextRes(recommendation: ActiveLaborRecommendation): Int = when (recommendation.source) {
    ActiveLaborRecommendationSource.CONTRACTIONS -> contractionRecommendationTextRes(recommendation.level)
    ActiveLaborRecommendationSource.WATER_BREAK_CLEAR -> R.string.active_labor_recommendation_water_text
    ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR -> R.string.active_labor_recommendation_urgent_text
    ActiveLaborRecommendationSource.BIRTH_RECORDED -> R.string.active_labor_recommendation_birth_text
}

private fun activeLaborTone(recommendation: ActiveLaborRecommendation): StatusTone = when (recommendation.source) {
    ActiveLaborRecommendationSource.BIRTH_RECORDED -> StatusTone.Success
    else -> contractionRecommendationTone(recommendation.level)
}

private fun waterColorLabelRes(color: WaterColor): Int = when (color) {
    WaterColor.CLEAR -> R.string.water_color_clear
    WaterColor.PINK -> R.string.water_color_pink
    WaterColor.GREEN -> R.string.water_color_green
    WaterColor.BROWN -> R.string.water_color_brown
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun ContractionPreview() {
    DadNavigatorTheme(dynamicColor = false) {
        ContractionContent(
            state = ContractionUiState(
                isSessionActive = true,
                activeContractionId = 1L,
                sessionDuration = Duration.ofMinutes(53),
                currentContractionDuration = Duration.ofSeconds(47),
                currentInterval = Duration.ofMinutes(4),
                stats = ContractionStats(
                    count = 6,
                    averageDuration = Duration.ofSeconds(58),
                    averageInterval = Duration.ofMinutes(4),
                    lastInterval = Duration.ofMinutes(3),
                    trend = ContractionTrend.BECOMING_MORE_INTENSE,
                    recommendationLevel = RecommendationLevel.PREPARE
                ),
                recommendation = ActiveLaborRecommendation(
                    level = RecommendationLevel.PREPARE,
                    source = ActiveLaborRecommendationSource.CONTRACTIONS
                ),
                latestWaterBreak = WaterBreakEvent(
                    id = 1L,
                    userId = "u",
                    happenedAt = Instant.now().minusSeconds(900),
                    color = WaterColor.CLEAR,
                    notes = "",
                    closedAt = null
                ),
                waterBreakElapsed = Duration.ofMinutes(15),
                contractions = listOf(
                    Contraction(1, 1, "u", Instant.now().minusSeconds(500), Instant.now().minusSeconds(450)),
                    Contraction(2, 1, "u", Instant.now().minusSeconds(300), Instant.now().minusSeconds(250))
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onPrimaryAction = {},
            onAskFinishSession = {},
            onMarkBirth = {},
            onOpenWaterBreak = {},
            onAskDeleteContraction = {}
        )
    }
}









