package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.TimelineEventEntity
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType

/**
 * Maps timeline entities.
 */
fun TimelineEventEntity.toDomain(): TimelineEvent = TimelineEvent(
    id = id,
    userId = userId,
    type = runCatching { TimelineType.valueOf(type) }.getOrDefault(TimelineType.NOTE),
    timestamp = timestamp,
    title = title,
    description = description
)

fun TimelineEvent.toEntity(): TimelineEventEntity = TimelineEventEntity(
    id = id,
    userId = userId,
    type = type.name,
    timestamp = timestamp,
    title = title,
    description = description
)
