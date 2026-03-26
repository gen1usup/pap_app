package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.model.ContractionSession
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Contract for managing contraction sessions and events.
 */
interface ContractionRepository {
    fun observeActiveState(userId: String): Flow<ActiveContractionState>

    fun observeSessionHistory(userId: String): Flow<List<ContractionSession>>

    suspend fun startSession(userId: String, startedAt: Instant): Long

    suspend fun finishSession(sessionId: Long, endedAt: Instant)

    suspend fun startContraction(sessionId: Long, userId: String, startedAt: Instant): Long

    suspend fun finishContraction(contractionId: Long, endedAt: Instant)
}
