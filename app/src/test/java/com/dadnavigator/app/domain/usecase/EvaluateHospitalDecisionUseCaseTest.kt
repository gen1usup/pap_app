package com.dadnavigator.app.domain.usecase

import com.dadnavigator.app.domain.model.DecisionReason
import com.dadnavigator.app.domain.model.HospitalDecisionInput
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.usecase.decision.EvaluateHospitalDecisionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for hospital decision tree.
 */
class EvaluateHospitalDecisionUseCaseTest {

    private val useCase = EvaluateHospitalDecisionUseCase()

    @Test
    fun `returns emergency when bleeding`() {
        val result = useCase(
            HospitalDecisionInput(
                contractionsLessThanFiveMinutes = false,
                contractionsLongerThanMinute = false,
                contractionsRegularForHour = false,
                waterBreak = false,
                bleeding = true,
                constantPain = false,
                feverOrWorseCondition = false,
                decreasedFetalMovement = false
            )
        )

        assertEquals(RecommendationLevel.EMERGENCY, result.level)
        assertTrue(result.reasons.contains(DecisionReason.EMERGENCY_BLEEDING))
    }

    @Test
    fun `returns go to hospital for regular active labor pattern`() {
        val result = useCase(
            HospitalDecisionInput(
                contractionsLessThanFiveMinutes = true,
                contractionsLongerThanMinute = true,
                contractionsRegularForHour = true,
                waterBreak = false,
                bleeding = false,
                constantPain = false,
                feverOrWorseCondition = false,
                decreasedFetalMovement = false
            )
        )

        assertEquals(RecommendationLevel.GO_TO_HOSPITAL, result.level)
        assertEquals(listOf(DecisionReason.REGULAR_LABOR), result.reasons)
    }

    @Test
    fun `returns monitor when no active signs`() {
        val result = useCase(
            HospitalDecisionInput(
                contractionsLessThanFiveMinutes = false,
                contractionsLongerThanMinute = false,
                contractionsRegularForHour = false,
                waterBreak = false,
                bleeding = false,
                constantPain = false,
                feverOrWorseCondition = false,
                decreasedFetalMovement = false
            )
        )

        assertEquals(RecommendationLevel.MONITOR, result.level)
        assertEquals(listOf(DecisionReason.MONITOR), result.reasons)
    }
}
