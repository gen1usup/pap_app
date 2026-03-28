package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import javax.inject.Inject

/**
 * Centralizes application stage transitions.
 *
 * The app now has only three stages:
 * PREPARING, LABOR and BABY_BORN.
 * Physical location is not encoded in stage anymore.
 */
class StageTransitionManager @Inject constructor() {

    @Suppress("UNUSED_PARAMETER")
    fun canSelectStage(targetStage: AppStage, currentSummary: LaborSummary): Boolean {
        return when (targetStage) {
            AppStage.PREPARING,
            AppStage.LABOR,
            AppStage.BABY_BORN -> true
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun manualSelection(
        targetStage: AppStage,
        currentStage: AppStage,
        currentSummary: LaborSummary
    ): ManualStageSelectionResult {
        return ManualStageSelectionResult(
            stage = if (currentStage == targetStage) currentStage else targetStage,
            blockedByBirthRecord = false
        )
    }

    fun laborStarted(currentSummary: LaborSummary): AppStage {
        return if (currentSummary.birthTime != null) {
            AppStage.BABY_BORN
        } else {
            AppStage.LABOR
        }
    }

    fun babyBorn(): AppStage = AppStage.BABY_BORN

}

data class ManualStageSelectionResult(
    val stage: AppStage,
    val blockedByBirthRecord: Boolean
)


