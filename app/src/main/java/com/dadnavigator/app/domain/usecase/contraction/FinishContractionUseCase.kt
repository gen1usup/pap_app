package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.repository.ContractionRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Finishes one ongoing contraction event.
 */
class FinishContractionUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    suspend operator fun invoke(contractionId: Long, endedAt: Instant = Instant.now()) {
        contractionRepository.finishContraction(contractionId, endedAt)
    }
}
