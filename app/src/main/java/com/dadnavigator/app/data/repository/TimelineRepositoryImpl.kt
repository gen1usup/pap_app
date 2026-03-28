package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.TimelineDao
import com.dadnavigator.app.data.local.entity.TimelineEventEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.data.mapper.toEntity
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Room-backed implementation for timeline records.
 */
class TimelineRepositoryImpl @Inject constructor(
    private val timelineDao: TimelineDao
) : TimelineRepository {

    override fun observeTimeline(userId: String): Flow<List<TimelineEvent>> {
        return timelineDao.observeTimeline(userId).map { events -> events.map { it.toDomain() } }
    }

    override suspend fun addEvent(event: TimelineEvent) {
        timelineDao.insertEvent(event.toEntity())
    }

    override suspend fun addEvent(
        userId: String,
        timestamp: Instant,
        title: String,
        description: String,
        type: TimelineType,
        stageAtCreation: AppStage,
        entryType: TimelineEntryType
    ) {
        timelineDao.insertEvent(
            TimelineEventEntity(
                userId = userId,
                type = type.name,
                timestamp = timestamp,
                title = title,
                description = description,
                stageAtCreation = stageAtCreation.name,
                entryType = entryType.name
            )
        )
    }
}
