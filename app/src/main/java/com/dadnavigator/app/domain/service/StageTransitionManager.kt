package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import javax.inject.Inject

/**
 * Centralizes explicit stage transitions triggered by user actions.
 */
class StageTransitionManager @Inject constructor() {

    fun manualSelection(targetStage: AppStage): AppStage = targetStage

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
