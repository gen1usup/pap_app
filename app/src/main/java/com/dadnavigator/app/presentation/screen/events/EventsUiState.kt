package com.dadnavigator.app.presentation.screen.events

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary

/**
 * UI state for the events hub.
 */
data class EventsUiState(
    val appStage: AppStage = AppStage.PREPARING,
    val hasActiveContractionSession: Boolean = false,
    val hasActiveWaterBreak: Boolean = false,
    val laborSummary: LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    ),
    val showBirthSheet: Boolean = false,
    val babyNameInput: String = "",
    val weightInput: String = "",
    val heightInput: String = "",
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
