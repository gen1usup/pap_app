package com.dadnavigator.app.presentation.screen.events

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.service.EventAction
import com.dadnavigator.app.domain.service.EventsContent

/**
 * UI state for the events hub.
 */
data class EventsUiState(
    val appStage: AppStage = AppStage.PREPARING,
    val hasActiveContractionSession: Boolean = false,
    val contractionSessionId: Long? = null,
    val isContractionRunning: Boolean = false,
    val activeContractionId: Long? = null,
    val hasActiveWaterBreak: Boolean = false,
    val content: EventsContent = EventsContent(
        sections = emptyList(),
        showBirthSummary = false
    ),
    val laborSummary: LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    ),
    val showBirthSheet: Boolean = false,
    val showQuickRecordSheet: Boolean = false,
    val activeQuickRecordAction: EventAction? = null,
    val quickRecordTitleInput: String = "",
    val quickRecordDescriptionInput: String = "",
    val babyNameInput: String = "",
    val weightInput: String = "",
    val heightInput: String = "",
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
