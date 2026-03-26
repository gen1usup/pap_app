package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.repository.ContractionRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Starts one contraction event.
 */
class StartContractionUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    suspend operator fun invoke(sessionId: Long, userId: String, startedAt: Instant = Instant.now()): Long {
        return contractionRepository.startContraction(sessionId, userId, startedAt)
    }
}
