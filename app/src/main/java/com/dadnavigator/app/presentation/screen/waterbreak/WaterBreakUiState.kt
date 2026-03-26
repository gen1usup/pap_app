package com.dadnavigator.app.presentation.screen.waterbreak

import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import java.time.Duration

/**
 * UI state for water break timer screen.
 */
data class WaterBreakUiState(
    val activeEvent: WaterBreakEvent? = null,
    val history: List<WaterBreakEvent> = emptyList(),
    val elapsed: Duration = Duration.ZERO,
    val selectedColor: WaterColor = WaterColor.CLEAR,
    val notes: String = "",
    val errorRes: Int? = null
)
