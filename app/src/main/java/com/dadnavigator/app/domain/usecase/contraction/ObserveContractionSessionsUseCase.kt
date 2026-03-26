package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.model.ContractionSession
import com.dadnavigator.app.domain.repository.ContractionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes completed and active contraction sessions.
 */
class ObserveContractionSessionsUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    operator fun invoke(userId: String): Flow<List<ContractionSession>> =
        contractionRepository.observeSessionHistory(userId)
}
