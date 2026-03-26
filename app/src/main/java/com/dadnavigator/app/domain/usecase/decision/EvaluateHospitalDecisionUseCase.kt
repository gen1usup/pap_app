package com.dadnavigator.app.domain.usecase.decision

import com.dadnavigator.app.domain.model.DecisionReason
import com.dadnavigator.app.domain.model.HospitalDecisionInput
import com.dadnavigator.app.domain.model.HospitalDecisionResult
import com.dadnavigator.app.domain.model.RecommendationLevel
import javax.inject.Inject

/**
 * Executes labor transport decision tree.
 */
class EvaluateHospitalDecisionUseCase @Inject constructor() {

    operator fun invoke(input: HospitalDecisionInput): HospitalDecisionResult {
        val reasons = mutableListOf<DecisionReason>()

        if (input.bleeding) reasons += DecisionReason.EMERGENCY_BLEEDING
        if (input.constantPain) reasons += DecisionReason.EMERGENCY_PAIN
        if (input.feverOrWorseCondition) reasons += DecisionReason.EMERGENCY_FEVER
        if (input.decreasedFetalMovement) reasons += DecisionReason.EMERGENCY_MOVEMENT

        if (reasons.isNotEmpty()) {
            return HospitalDecisionResult(
                level = RecommendationLevel.EMERGENCY,
                reasons = reasons
            )
        }

        if (input.waterBreak) {
            return HospitalDecisionResult(
                level = RecommendationLevel.GO_TO_HOSPITAL,
                reasons = listOf(DecisionReason.WATER_BREAK)
            )
        }

        if (
            input.contractionsLessThanFiveMinutes &&
            input.contractionsLongerThanMinute &&
            input.contractionsRegularForHour
        ) {
            return HospitalDecisionResult(
                level = RecommendationLevel.GO_TO_HOSPITAL,
                reasons = listOf(DecisionReason.REGULAR_LABOR)
            )
        }

        if (
            input.contractionsLessThanFiveMinutes ||
            input.contractionsLongerThanMinute ||
            input.contractionsRegularForHour
        ) {
            return HospitalDecisionResult(
                level = RecommendationLevel.PREPARE,
                reasons = listOf(DecisionReason.PREPARE)
            )
        }

        return HospitalDecisionResult(
            level = RecommendationLevel.MONITOR,
            reasons = listOf(DecisionReason.MONITOR)
        )
    }
}
