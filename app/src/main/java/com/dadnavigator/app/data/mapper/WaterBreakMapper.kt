package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.WaterBreakEventEntity
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor

/**
 * Maps water break entities.
 */
fun WaterBreakEventEntity.toDomain(): WaterBreakEvent = WaterBreakEvent(
    id = id,
    userId = userId,
    happenedAt = happenedAt,
    color = runCatching { WaterColor.valueOf(color) }.getOrDefault(WaterColor.CLEAR),
    notes = notes,
    closedAt = closedAt
)

fun WaterBreakEvent.toEntity(): WaterBreakEventEntity = WaterBreakEventEntity(
    id = id,
    userId = userId,
    happenedAt = happenedAt,
    color = color.name,
    notes = notes,
    closedAt = closedAt
)
