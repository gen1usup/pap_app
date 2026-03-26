package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes app settings stream.
 */
class ObserveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> = settingsRepository.observeSettings()
}
