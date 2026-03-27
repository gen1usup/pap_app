package com.dadnavigator.app.presentation.screen.checklist

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ChecklistWithItems

/**
 * UI state for checklist screen.
 */
data class ChecklistUiState(
    val checklists: List<ChecklistWithItems> = emptyList(),
    val selectedStage: AppStage = AppStage.PREPARING,
    val currentStage: AppStage = AppStage.PREPARING,
    val drafts: Map<Long, String> = emptyMap(),
    val errorRes: Int? = null
)
