package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Persists settings updates.
 */
class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: Settings) {
        settingsRepository.saveSettings(settings)
    }
}
