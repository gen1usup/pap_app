package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.DiaperLogEntity
import com.dadnavigator.app.data.local.entity.FeedingLogEntity
import com.dadnavigator.app.data.local.entity.NoteEntity
import com.dadnavigator.app.data.local.entity.SleepLogEntity
import com.dadnavigator.app.domain.model.DiaperLog
import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.model.FeedingLog
import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.domain.model.Note
import com.dadnavigator.app.domain.model.SleepLog

/**
 * Maps tracker entities.
 */
fun FeedingLogEntity.toDomain(): FeedingLog = FeedingLog(
    id = id,
    userId = userId,
    timestamp = timestamp,
    durationMinutes = durationMinutes,
    type = runCatching { FeedingType.valueOf(type) }.getOrDefault(FeedingType.LEFT)
)

fun FeedingLog.toEntity(): FeedingLogEntity = FeedingLogEntity(
    id = id,
    userId = userId,
    timestamp = timestamp,
    durationMinutes = durationMinutes,
    type = type.name
)

fun DiaperLogEntity.toDomain(): DiaperLog = DiaperLog(
    id = id,
    userId = userId,
    timestamp = timestamp,
    type = runCatching { DiaperType.valueOf(type) }.getOrDefault(DiaperType.WET),
    notes = notes
)

fun DiaperLog.toEntity(): DiaperLogEntity = DiaperLogEntity(
    id = id,
    userId = userId,
    timestamp = timestamp,
    type = type.name,
    notes = notes
)

fun SleepLogEntity.toDomain(): SleepLog = SleepLog(
    id = id,
    userId = userId,
    startTime = startTime,
    endTime = endTime,
    notes = notes
)

fun SleepLog.toEntity(): SleepLogEntity = SleepLogEntity(
    id = id,
    userId = userId,
    startTime = startTime,
    endTime = endTime,
    notes = notes
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    userId = userId,
    timestamp = timestamp,
    text = text,
    category = category
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    userId = userId,
    timestamp = timestamp,
    text = text,
    category = category
)
