package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadnavigator.app.data.local.entity.LaborSummaryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for labor summary.
 */
@Dao
interface LaborDao {

    @Query("SELECT * FROM labor_summary WHERE userId = :userId LIMIT 1")
    fun observeLaborSummary(userId: String): Flow<LaborSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLaborSummary(entity: LaborSummaryEntity)

    @Query("DELETE FROM labor_summary")
    suspend fun clearLaborSummary()
}
