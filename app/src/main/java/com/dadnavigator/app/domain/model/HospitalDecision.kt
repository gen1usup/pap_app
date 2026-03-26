package com.dadnavigator.app.domain.model

/**
 * Input for labor transport decision tree.
 */
data class HospitalDecisionInput(
    val contractionsLessThanFiveMinutes: Boolean,
    val contractionsLongerThanMinute: Boolean,
    val contractionsRegularForHour: Boolean,
    val waterBreak: Boolean,
    val bleeding: Boolean,
    val constantPain: Boolean,
    val feverOrWorseCondition: Boolean,
    val decreasedFetalMovement: Boolean
)

/**
 * Result of decision tree execution.
 */
data class HospitalDecisionResult(
    val level: RecommendationLevel,
    val reasons: List<DecisionReason>
)

/**
 * Atomic reasoning unit to map to localized text in presentation layer.
 */
enum class DecisionReason {
    EMERGENCY_BLEEDING,
    EMERGENCY_PAIN,
    EMERGENCY_FEVER,
    EMERGENCY_MOVEMENT,
    WATER_BREAK,
    REGULAR_LABOR,
    PREPARE,
    MONITOR
}
