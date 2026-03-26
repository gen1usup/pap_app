package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Clears local persisted user data.
 */
class ResetAllDataUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.clearAllData()
    }
}
