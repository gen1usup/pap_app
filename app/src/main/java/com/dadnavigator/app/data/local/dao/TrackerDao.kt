package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadnavigator.app.data.local.entity.DiaperLogEntity
import com.dadnavigator.app.data.local.entity.FeedingLogEntity
import com.dadnavigator.app.data.local.entity.NoteEntity
import com.dadnavigator.app.data.local.entity.SleepLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for postpartum trackers.
 */
@Dao
interface TrackerDao {

    @Query("SELECT * FROM feeding_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeFeedingLogs(userId: String): Flow<List<FeedingLogEntity>>

    @Query("SELECT * FROM diaper_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeDiaperLogs(userId: String): Flow<List<DiaperLogEntity>>

    @Query("SELECT * FROM sleep_logs WHERE userId = :userId ORDER BY startTime DESC")
    fun observeSleepLogs(userId: String): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeNotes(userId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedingLog(entity: FeedingLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaperLog(entity: DiaperLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(entity: SleepLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(entity: NoteEntity)

    @Query("DELETE FROM feeding_logs")
    suspend fun clearFeeding()

    @Query("DELETE FROM diaper_logs")
    suspend fun clearDiaper()

    @Query("DELETE FROM sleep_logs")
    suspend fun clearSleep()

    @Query("DELETE FROM notes")
    suspend fun clearNotes()
}
