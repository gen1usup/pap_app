package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.ContractionEntity
import com.dadnavigator.app.data.local.entity.ContractionSessionEntity
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionSession

/**
 * Maps contraction entities to domain models and back.
 */
fun ContractionSessionEntity.toDomain(): ContractionSession = ContractionSession(
    id = id,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt
)

fun ContractionSession.toEntity(): ContractionSessionEntity = ContractionSessionEntity(
    id = id,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt
)

fun ContractionEntity.toDomain(): Contraction = Contraction(
    id = id,
    sessionId = sessionId,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt
)

fun Contraction.toEntity(): ContractionEntity = ContractionEntity(
    id = id,
    sessionId = sessionId,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt
)
