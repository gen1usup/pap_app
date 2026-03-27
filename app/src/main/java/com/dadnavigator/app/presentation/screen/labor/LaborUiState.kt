package com.dadnavigator.app.presentation.screen.labor

import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.TimelineEvent

/**
 * UI state for labor screen.
 */
data class LaborUiState(
    val summary: LaborSummary = LaborSummary(null, null, null, null, null),
    val laborEvents: List<TimelineEvent> = emptyList(),
    val babyNameInput: String = "",
    val weightInput: String = "",
    val heightInput: String = "",
    val customEventTitle: String = "",
    val customEventNote: String = "",
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
