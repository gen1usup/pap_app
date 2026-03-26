package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.repository.ContractionRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Starts a new contraction session.
 */
class StartContractionSessionUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    suspend operator fun invoke(userId: String, startedAt: Instant = Instant.now()): Long {
        return contractionRepository.startSession(userId, startedAt)
    }
}
