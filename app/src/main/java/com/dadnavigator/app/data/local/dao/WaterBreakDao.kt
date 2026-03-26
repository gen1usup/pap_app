package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadnavigator.app.data.local.entity.WaterBreakEventEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * DAO for water break events.
 */
@Dao
interface WaterBreakDao {

    @Query(
        """
        SELECT * FROM water_break_events
        WHERE userId = :userId AND closedAt IS NULL
        ORDER BY happenedAt DESC
        LIMIT 1
        """
    )
    suspend fun getActiveEvent(userId: String): WaterBreakEventEntity?

    @Query(
        """
        SELECT * FROM water_break_events
        WHERE userId = :userId AND closedAt IS NULL
        ORDER BY happenedAt DESC
        LIMIT 1
        """
    )
    fun observeActiveEvent(userId: String): Flow<WaterBreakEventEntity?>

    @Query(
        """
        SELECT * FROM water_break_events
        WHERE userId = :userId
        ORDER BY happenedAt DESC
        """
    )
    fun observeHistory(userId: String): Flow<List<WaterBreakEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(entity: WaterBreakEventEntity): Long

    @Query(
        """
        UPDATE water_break_events
        SET closedAt = :closedAt
        WHERE userId = :userId AND closedAt IS NULL
        """
    )
    suspend fun closeActiveEvent(userId: String, closedAt: Instant)
}
