package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.AppDatabase
import com.dadnavigator.app.data.local.dao.SettingsDao
import com.dadnavigator.app.data.local.dao.UserDao
import com.dadnavigator.app.data.local.datastore.SettingsDataStore
import com.dadnavigator.app.data.mapper.toEntity
import com.dadnavigator.app.domain.model.AppUser
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.repository.SettingsRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Hybrid settings repository using DataStore as source of truth and Room as sync-ready mirror.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val settingsDao: SettingsDao,
    private val userDao: UserDao,
    private val database: AppDatabase
) : SettingsRepository {

    override fun observeSettings(): Flow<Settings> = dataStore.observeSettings()

    override suspend fun saveSettings(settings: Settings) {
        dataStore.saveSettings(settings)
        settingsDao.upsertSettings(settings.toEntity())
        userDao.upsertUser(
            AppUser(
                id = settings.userId.ifBlank { DEFAULT_USER_ID },
                displayName = settings.fatherName,
                createdAt = Instant.now()
            ).toEntity()
        )
    }

    override suspend fun clearAllData() {
        database.clearAllTables()
        dataStore.clear()
    }
}
