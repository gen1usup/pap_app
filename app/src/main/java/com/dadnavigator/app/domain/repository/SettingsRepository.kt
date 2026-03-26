package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Contract for user preferences and app-level settings.
 */
interface SettingsRepository {
    fun observeSettings(): Flow<Settings>

    suspend fun saveSettings(settings: Settings)

    suspend fun clearAllData()
}
