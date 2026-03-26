package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadnavigator.app.data.local.entity.TimelineEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for unified timeline.
 */
@Dao
interface TimelineDao {

    @Query(
        """
        SELECT * FROM timeline_events
        WHERE userId = :userId
        ORDER BY timestamp DESC
        """
    )
    fun observeTimeline(userId: String): Flow<List<TimelineEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(entity: TimelineEventEntity)

    @Query("DELETE FROM timeline_events")
    suspend fun clear()
}
