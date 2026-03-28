package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.model.defaultEntryType
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Contract for unified chronology events.
 */
interface TimelineRepository {
    fun observeTimeline(userId: String): Flow<List<TimelineEvent>>

    suspend fun addEvent(event: TimelineEvent)

    suspend fun addEvent(
        userId: String,
        timestamp: Instant,
        title: String,
        description: String,
        type: TimelineType,
        stageAtCreation: AppStage,
        entryType: TimelineEntryType = type.defaultEntryType()
    )
}
