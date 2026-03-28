package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.TimelineEventEntity
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.model.defaultEntryType

/**
 * Maps timeline entities.
 */
fun TimelineEventEntity.toDomain(): TimelineEvent {
    val resolvedType = when (type) {
        "HOSPITAL_NOTE",
        "HOME_NOTE" -> TimelineType.BABY_NOTE
        else -> runCatching { TimelineType.valueOf(type) }.getOrDefault(TimelineType.NOTE)
    }
    val resolvedStage = when (type) {
        "HOSPITAL_NOTE",
        "HOME_NOTE" -> AppStage.BABY_BORN
        else -> AppStage.fromStorage(stageAtCreation)
    }
    return TimelineEvent(
        id = id,
        userId = userId,
        type = resolvedType,
        timestamp = timestamp,
        title = title,
        description = description,
        stageAtCreation = resolvedStage,
        entryType = runCatching { TimelineEntryType.valueOf(entryType) }
            .getOrDefault(resolvedType.defaultEntryType())
    )
}

fun TimelineEvent.toEntity(): TimelineEventEntity = TimelineEventEntity(
    id = id,
    userId = userId,
    type = type.name,
    timestamp = timestamp,
    title = title,
    description = description,
    stageAtCreation = stageAtCreation.name,
    entryType = entryType.name
)
