package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadnavigator.app.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for sync-ready settings snapshots.
 */
@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE userId = :userId LIMIT 1")
    fun observeSettings(userId: String): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(entity: SettingsEntity)

    @Query("DELETE FROM settings")
    suspend fun clearSettings()
}
