package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Contract for tracking amniotic fluid events.
 */
interface WaterBreakRepository {
    fun observeActiveEvent(userId: String): Flow<WaterBreakEvent?>

    fun observeHistory(userId: String): Flow<List<WaterBreakEvent>>

    suspend fun createEvent(
        userId: String,
        happenedAt: Instant,
        color: WaterColor,
        notes: String
    ): Long

    suspend fun closeActiveEvent(userId: String, closedAt: Instant)

    suspend fun deleteEvent(eventId: Long)
}
