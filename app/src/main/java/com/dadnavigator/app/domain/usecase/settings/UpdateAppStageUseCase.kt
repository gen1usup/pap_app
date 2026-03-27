package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.service.ManualStageSelectionResult
import com.dadnavigator.app.domain.service.StageTransitionManager
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Updates the manually selected app stage while preserving the rest of settings.
 */
class UpdateAppStageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val laborRepository: LaborRepository,
    private val stageTransitionManager: StageTransitionManager
) {
    suspend operator fun invoke(stage: AppStage): ManualStageSelectionResult {
        val current = settingsRepository.observeSettings().first()
        val userId = current.userId.ifBlank { DEFAULT_USER_ID }
        val currentSummary = laborRepository.observeLaborSummary(userId).first()
        val decision = stageTransitionManager.manualSelection(
            targetStage = stage,
            currentStage = current.appStage,
            currentSummary = currentSummary
        )
        settingsRepository.saveSettings(
            current.copy(appStage = decision.stage)
        )
        return decision
    }
}
