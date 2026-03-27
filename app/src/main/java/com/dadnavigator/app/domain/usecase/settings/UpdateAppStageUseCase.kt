package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Updates the manually selected app stage while preserving the rest of settings.
 */
class UpdateAppStageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val stageTransitionManager: StageTransitionManager
) {
    suspend operator fun invoke(stage: AppStage) {
        val current = settingsRepository.observeSettings().first()
        settingsRepository.saveSettings(
            current.copy(appStage = stageTransitionManager.manualSelection(stage))
        )
    }
}
