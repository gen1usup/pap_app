package com.dadnavigator.app.presentation.screen.decision

import com.dadnavigator.app.domain.model.HospitalDecisionResult

/**
 * UI state for hospital decision screen.
 */
data class DecisionUiState(
    val contractionsLessThanFiveMinutes: Boolean = false,
    val contractionsLongerThanMinute: Boolean = false,
    val contractionsRegularForHour: Boolean = false,
    val waterBreak: Boolean = false,
    val bleeding: Boolean = false,
    val constantPain: Boolean = false,
    val feverOrWorseCondition: Boolean = false,
    val decreasedFetalMovement: Boolean = false,
    val result: HospitalDecisionResult? = null
)
