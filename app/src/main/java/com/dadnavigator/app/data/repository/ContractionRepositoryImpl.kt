package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.ContractionDao
import com.dadnavigator.app.data.local.entity.ContractionEntity
import com.dadnavigator.app.data.local.entity.ContractionSessionEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.repository.ContractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Room-backed implementation for contraction tracking.
 */
class ContractionRepositoryImpl @Inject constructor(
    private val contractionDao: ContractionDao
) : ContractionRepository {

    override fun observeActiveState(userId: String): Flow<ActiveContractionState> {
        return contractionDao.observeActiveSession(userId).flatMapLatest { activeSession ->
            if (activeSession == null) {
                flowOf(ActiveContractionState(session = null, contractions = emptyList(), activeContraction = null))
            } else {
                combine(
                    contractionDao.observeContractions(activeSession.id),
                    contractionDao.observeActiveContraction(activeSession.id)
                ) { contractions, activeContraction ->
                    ActiveContractionState(
                        session = activeSession.toDomain(),
                        contractions = contractions.map { it.toDomain() },
                        activeContraction = activeContraction?.toDomain()
                    )
                }
            }
        }
    }

    override fun observeSessionHistory(userId: String): Flow<List<com.dadnavigator.app.domain.model.ContractionSession>> {
        return contractionDao.observeSessions(userId).map { sessions ->
            sessions.map { it.toDomain() }
        }
    }

    override suspend fun startSession(userId: String, startedAt: Instant): Long {
        val existing = contractionDao.getActiveSession(userId)
        if (existing != null) return existing.id
        return contractionDao.insertSession(
            ContractionSessionEntity(
                userId = userId,
                startedAt = startedAt,
                endedAt = null
            )
        )
    }

    override suspend fun finishSession(sessionId: Long, endedAt: Instant) {
        contractionDao.finishActiveContractionsInSession(sessionId, endedAt)
        contractionDao.finishSession(sessionId, endedAt)
    }

    override suspend fun startContraction(sessionId: Long, userId: String, startedAt: Instant): Long {
        val existing = contractionDao.getActiveContraction(sessionId)
        if (existing != null) return existing.id
        return contractionDao.insertContraction(
            ContractionEntity(
                sessionId = sessionId,
                userId = userId,
                startedAt = startedAt,
                endedAt = null
            )
        )
    }

    override suspend fun finishContraction(contractionId: Long, endedAt: Instant) {
        contractionDao.finishContraction(contractionId, endedAt)
    }
}
