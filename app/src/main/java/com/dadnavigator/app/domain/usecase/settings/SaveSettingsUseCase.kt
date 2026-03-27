package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.service.ManualStageSelectionResult
import com.dadnavigator.app.domain.service.StageTransitionManager
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Persists settings updates.
 */
class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val laborRepository: LaborRepository,
    private val stageTransitionManager: StageTransitionManager
) {
    suspend operator fun invoke(settings: Settings): ManualStageSelectionResult {
        val currentSettings = settingsRepository.observeSettings().first()
        val userId = settings.userId.ifBlank {
            currentSettings.userId.ifBlank { DEFAULT_USER_ID }
        }
        val currentSummary = laborRepository.observeLaborSummary(userId).first()
        val decision = stageTransitionManager.manualSelection(
            targetStage = settings.appStage,
            currentStage = currentSettings.appStage,
            currentSummary = currentSummary
        )
        settingsRepository.saveSettings(
            settings.copy(appStage = decision.stage)
        )
        return decision
    }
}
