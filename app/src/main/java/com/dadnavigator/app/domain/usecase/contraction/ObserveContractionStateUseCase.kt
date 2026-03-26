package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.repository.ContractionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes active contraction session state.
 */
class ObserveContractionStateUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    operator fun invoke(userId: String): Flow<ActiveContractionState> =
        contractionRepository.observeActiveState(userId)
}
