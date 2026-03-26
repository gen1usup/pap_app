package com.dadnavigator.app.presentation.screen.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.DecisionReason
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone

@Composable
fun DecisionScreen(
    onBack: () -> Unit,
    viewModel: DecisionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val spacing = DadTheme.spacing

    ScreenScaffold(
        title = stringResource(id = R.string.decision_title),
        subtitle = stringResource(id = R.string.decision_subtitle),
        onBack = onBack
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
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_interval),
                        value = state.contractionsLessThanFiveMinutes,
                        onValueChanged = viewModel::setContractionsLessThanFiveMinutes
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_duration),
                        value = state.contractionsLongerThanMinute,
                        onValueChanged = viewModel::setContractionsLongerThanMinute
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_regular),
                        value = state.contractionsRegularForHour,
                        onValueChanged = viewModel::setContractionsRegularForHour
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_water_break),
                        value = state.waterBreak,
                        onValueChanged = viewModel::setWaterBreak
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_bleeding),
                        value = state.bleeding,
                        onValueChanged = viewModel::setBleeding
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_pain),
                        value = state.constantPain,
                        onValueChanged = viewModel::setConstantPain
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_fever),
                        value = state.feverOrWorseCondition,
                        onValueChanged = viewModel::setFeverOrWorseCondition
                    )
                }
                item {
                    DecisionQuestionCard(
                        title = stringResource(id = R.string.decision_question_movement),
                        value = state.decreasedFetalMovement,
                        onValueChanged = viewModel::setDecreasedFetalMovement
                    )
                }
                item {
                    PrimaryButton(
                        text = stringResource(id = R.string.decision_calculate),
                        onClick = viewModel::calculate,
                        icon = Icons.Outlined.LocalHospital
                    )
                }
                val result = state.result
                if (result != null) {
                    item {
                        StatusCard(
                            title = stringResource(id = recommendationTitleRes(result.level)),
                            description = stringResource(id = recommendationTextRes(result.level)),
                            tone = recommendationTone(result.level),
                            icon = Icons.Outlined.LocalHospital,
                            headline = stringResource(id = R.string.decision_result)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                                Text(
                                    text = stringResource(id = R.string.decision_explanation),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                result.reasons.forEach { reason ->
                                    Text(
                                        text = "• ${stringResource(id = reasonTextRes(reason))}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DecisionQuestionCard(
    title: String,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit
) {
    InfoCard(
        title = title,
        description = stringResource(id = R.string.decision_boolean_hint),
        icon = Icons.Outlined.LocalHospital
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(DadTheme.spacing.sm)) {
            FilterChip(
                selected = value,
                onClick = { onValueChanged(true) },
                label = { Text(text = stringResource(id = R.string.yes)) }
            )
            FilterChip(
                selected = !value,
                onClick = { onValueChanged(false) },
                label = { Text(text = stringResource(id = R.string.no)) }
            )
        }
    }
}

private fun recommendationTitleRes(level: RecommendationLevel): Int = when (level) {
    RecommendationLevel.MONITOR -> R.string.contraction_stage_monitor
    RecommendationLevel.PREPARE -> R.string.contraction_stage_prepare
    RecommendationLevel.GO_TO_HOSPITAL -> R.string.contraction_stage_go
    RecommendationLevel.EMERGENCY -> R.string.contraction_stage_emergency
}

private fun recommendationTextRes(level: RecommendationLevel): Int = when (level) {
    RecommendationLevel.MONITOR -> R.string.recommendation_monitor
    RecommendationLevel.PREPARE -> R.string.recommendation_prepare
    RecommendationLevel.GO_TO_HOSPITAL -> R.string.recommendation_go_hospital
    RecommendationLevel.EMERGENCY -> R.string.recommendation_emergency
}

private fun recommendationTone(level: RecommendationLevel): StatusTone = when (level) {
    RecommendationLevel.MONITOR -> StatusTone.Calm
    RecommendationLevel.PREPARE -> StatusTone.Warning
    RecommendationLevel.GO_TO_HOSPITAL -> StatusTone.Warning
    RecommendationLevel.EMERGENCY -> StatusTone.Critical
}

private fun reasonTextRes(reason: DecisionReason): Int = when (reason) {
    DecisionReason.EMERGENCY_BLEEDING -> R.string.decision_reason_emergency_bleeding
    DecisionReason.EMERGENCY_PAIN -> R.string.decision_reason_emergency_pain
    DecisionReason.EMERGENCY_FEVER -> R.string.decision_reason_emergency_fever
    DecisionReason.EMERGENCY_MOVEMENT -> R.string.decision_reason_emergency_movement
    DecisionReason.WATER_BREAK -> R.string.decision_reason_water_break
    DecisionReason.REGULAR_LABOR -> R.string.decision_reason_regular_labor
    DecisionReason.PREPARE -> R.string.decision_reason_prepare
    DecisionReason.MONITOR -> R.string.decision_reason_monitor
}
