package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.WaterBreakDao
import com.dadnavigator.app.data.local.entity.WaterBreakEventEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Room-backed implementation for water break tracking.
 */
class WaterBreakRepositoryImpl @Inject constructor(
    private val waterBreakDao: WaterBreakDao
) : WaterBreakRepository {

    override fun observeActiveEvent(userId: String): Flow<WaterBreakEvent?> {
        return waterBreakDao.observeActiveEvent(userId).map { it?.toDomain() }
    }

    override fun observeHistory(userId: String): Flow<List<WaterBreakEvent>> {
        return waterBreakDao.observeHistory(userId).map { events ->
            events.map { it.toDomain() }
        }
    }

    override suspend fun createEvent(
        userId: String,
        happenedAt: Instant,
        color: WaterColor,
        notes: String
    ): Long {
        val active = waterBreakDao.getActiveEvent(userId)
        if (active != null) {
            waterBreakDao.closeActiveEvent(userId, happenedAt)
        }
        return waterBreakDao.insertEvent(
            WaterBreakEventEntity(
                userId = userId,
                happenedAt = happenedAt,
                color = color.name,
                notes = notes,
                closedAt = null
            )
        )
    }

    override suspend fun closeActiveEvent(userId: String, closedAt: Instant) {
        waterBreakDao.closeActiveEvent(userId, closedAt)
    }
}
