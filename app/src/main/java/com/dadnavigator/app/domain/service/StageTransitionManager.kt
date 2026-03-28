package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import javax.inject.Inject

/**
 * Centralizes explicit stage transitions triggered by user actions.
 */
class StageTransitionManager @Inject constructor() {

    fun canSelectStage(targetStage: AppStage, currentSummary: LaborSummary): Boolean {
        return true
    }

    fun manualSelection(
        targetStage: AppStage,
        currentStage: AppStage,
        currentSummary: LaborSummary
    ): ManualStageSelectionResult {
        return ManualStageSelectionResult(
            stage = targetStage,
            blockedByBirthRecord = false
        )
    }

    fun laborStarted(currentSummary: LaborSummary): AppStage {
        return if (currentSummary.birthTime != null) {
            AppStage.AT_HOSPITAL
        } else {
            AppStage.CONTRACTIONS
        }
    }

    fun babyBorn(): AppStage = AppStage.AT_HOSPITAL

    fun arrivedHome(): AppStage = AppStage.AT_HOME

}

data class ManualStageSelectionResult(
    val stage: AppStage,
    val blockedByBirthRecord: Boolean
)
