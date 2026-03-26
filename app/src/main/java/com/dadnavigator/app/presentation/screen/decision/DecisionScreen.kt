package com.dadnavigator.app.presentation.screen.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.DecisionReason
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Decision tree screen for the "go to hospital" recommendation.
 */
@Composable
fun DecisionScreen(
    onBack: () -> Unit,
    viewModel: DecisionViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ScreenScaffold(
        title = stringResource(id = R.string.decision_title),
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
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_interval),
                    value = state.contractionsLessThanFiveMinutes,
                    onValueChanged = viewModel::setContractionsLessThanFiveMinutes
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_duration),
                    value = state.contractionsLongerThanMinute,
                    onValueChanged = viewModel::setContractionsLongerThanMinute
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_regular),
                    value = state.contractionsRegularForHour,
                    onValueChanged = viewModel::setContractionsRegularForHour
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_water_break),
                    value = state.waterBreak,
                    onValueChanged = viewModel::setWaterBreak
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_bleeding),
                    value = state.bleeding,
                    onValueChanged = viewModel::setBleeding
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_pain),
                    value = state.constantPain,
                    onValueChanged = viewModel::setConstantPain
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_fever),
                    value = state.feverOrWorseCondition,
                    onValueChanged = viewModel::setFeverOrWorseCondition
                )
            }
            item {
                DecisionQuestionRow(
                    title = stringResource(id = R.string.decision_question_movement),
                    value = state.decreasedFetalMovement,
                    onValueChanged = viewModel::setDecreasedFetalMovement
                )
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::calculate
                ) {
                    Text(text = stringResource(id = R.string.decision_calculate))
                }
            }

            val result = state.result
            if (result != null) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = stringResource(id = R.string.decision_result), style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(id = recommendationTextRes(result.level)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(text = stringResource(id = R.string.decision_explanation), style = MaterialTheme.typography.titleSmall)
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

@Composable
private fun DecisionQuestionRow(
    title: String,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
}

private fun recommendationTextRes(level: RecommendationLevel): Int = when (level) {
    RecommendationLevel.MONITOR -> R.string.recommendation_monitor
    RecommendationLevel.PREPARE -> R.string.recommendation_prepare
    RecommendationLevel.GO_TO_HOSPITAL -> R.string.recommendation_go_hospital
    RecommendationLevel.EMERGENCY -> R.string.recommendation_emergency
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
