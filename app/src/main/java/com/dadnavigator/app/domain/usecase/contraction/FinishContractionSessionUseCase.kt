package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.repository.ContractionRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Finishes an active contraction session.
 */
class FinishContractionSessionUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    suspend operator fun invoke(sessionId: Long, endedAt: Instant = Instant.now()) {
        contractionRepository.finishSession(sessionId, endedAt)
    }
}
