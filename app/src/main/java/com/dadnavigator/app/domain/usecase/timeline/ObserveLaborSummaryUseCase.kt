package com.dadnavigator.app.domain.usecase.timeline

import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.repository.LaborRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes labor summary.
 */
class ObserveLaborSummaryUseCase @Inject constructor(
    private val laborRepository: LaborRepository
) {
    operator fun invoke(userId: String): Flow<LaborSummary> = laborRepository.observeLaborSummary(userId)
}
