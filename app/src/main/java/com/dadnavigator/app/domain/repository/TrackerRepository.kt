package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.DiaperLog
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingLog
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.domain.model.Note
import com.dadnavigator.app.domain.model.SleepLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Contract for postpartum trackers.
 */
interface TrackerRepository {
    fun observeFeedingLogs(userId: String): Flow<List<FeedingLog>>

    fun observeDiaperLogs(userId: String): Flow<List<DiaperLog>>

    fun observeSleepLogs(userId: String): Flow<List<SleepLog>>

    fun observeNotes(userId: String): Flow<List<Note>>

    suspend fun addFeedingLog(
        userId: String,
        timestamp: Instant,
        durationMinutes: Int,
        type: FeedingType
    )

    suspend fun addDiaperLog(
        userId: String,
        timestamp: Instant,
        type: DiaperType,
        notes: String
    )

    suspend fun addSleepLog(
        userId: String,
        startTime: Instant,
        endTime: Instant,
        notes: String
    )

    suspend fun addNote(
        userId: String,
        timestamp: Instant,
        text: String,
        category: String
    )
}
