package com.dadnavigator.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore data source for settings.
 */
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val userIdKey = stringPreferencesKey("user_id")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val fatherNameKey = stringPreferencesKey("father_name")
    private val dueDateEpochDayKey = longPreferencesKey("due_date_epoch_day")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")

    fun observeSettings(): Flow<Settings> {
        return context.settingsDataStore.data.map { preferences ->
            Settings(
                userId = preferences[userIdKey] ?: DEFAULT_USER_ID,
                themeMode = ThemeMode.valueOf(preferences[themeModeKey] ?: ThemeMode.SYSTEM.name),
                fatherName = preferences[fatherNameKey].orEmpty(),
                dueDate = preferences[dueDateEpochDayKey]?.let(LocalDate::ofEpochDay),
                notificationsEnabled = preferences[notificationsEnabledKey] ?: true
            )
        }
    }

    suspend fun saveSettings(settings: Settings) {
        context.settingsDataStore.edit { preferences ->
            preferences[userIdKey] = settings.userId
            preferences[themeModeKey] = settings.themeMode.name
            preferences[fatherNameKey] = settings.fatherName
            if (settings.dueDate == null) {
                preferences.remove(dueDateEpochDayKey)
            } else {
                preferences[dueDateEpochDayKey] = settings.dueDate.toEpochDay()
            }
            preferences[notificationsEnabledKey] = settings.notificationsEnabled
        }
    }

    suspend fun clear() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
