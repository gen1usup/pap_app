package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.TrackerDao
import com.dadnavigator.app.data.local.entity.DiaperLogEntity
import com.dadnavigator.app.data.local.entity.FeedingLogEntity
import com.dadnavigator.app.data.local.entity.NoteEntity
import com.dadnavigator.app.data.local.entity.SleepLogEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.domain.model.DiaperLog
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingLog
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.domain.model.Note
import com.dadnavigator.app.domain.model.SleepLog
import com.dadnavigator.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Room-backed implementation for postpartum trackers.
 */
class TrackerRepositoryImpl @Inject constructor(
    private val trackerDao: TrackerDao
) : TrackerRepository {

    override fun observeFeedingLogs(userId: String): Flow<List<FeedingLog>> {
        return trackerDao.observeFeedingLogs(userId).map { items -> items.map { it.toDomain() } }
    }

    override fun observeDiaperLogs(userId: String): Flow<List<DiaperLog>> {
        return trackerDao.observeDiaperLogs(userId).map { items -> items.map { it.toDomain() } }
    }

    override fun observeSleepLogs(userId: String): Flow<List<SleepLog>> {
        return trackerDao.observeSleepLogs(userId).map { items -> items.map { it.toDomain() } }
    }

    override fun observeNotes(userId: String): Flow<List<Note>> {
        return trackerDao.observeNotes(userId).map { items -> items.map { it.toDomain() } }
    }

    override suspend fun addFeedingLog(
        userId: String,
        timestamp: Instant,
        durationMinutes: Int,
        type: FeedingType
    ) {
        trackerDao.insertFeedingLog(
            FeedingLogEntity(
                userId = userId,
                timestamp = timestamp,
                durationMinutes = durationMinutes,
                type = type.name
            )
        )
    }

    override suspend fun addDiaperLog(userId: String, timestamp: Instant, type: DiaperType, notes: String) {
        trackerDao.insertDiaperLog(
            DiaperLogEntity(
                userId = userId,
                timestamp = timestamp,
                type = type.name,
                notes = notes
            )
        )
    }

    override suspend fun addSleepLog(userId: String, startTime: Instant, endTime: Instant, notes: String) {
        trackerDao.insertSleepLog(
            SleepLogEntity(
                userId = userId,
                startTime = startTime,
                endTime = endTime,
                notes = notes
            )
        )
    }

    override suspend fun addNote(userId: String, timestamp: Instant, text: String, category: String) {
        trackerDao.insertNote(
            NoteEntity(
                userId = userId,
                timestamp = timestamp,
                text = text,
                category = category
            )
        )
    }
}
