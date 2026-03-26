package com.dadnavigator.app.domain.usecase.timeline

import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.repository.LaborRepository
import javax.inject.Inject

/**
 * Saves labor summary fields.
 */
class SaveLaborSummaryUseCase @Inject constructor(
    private val laborRepository: LaborRepository
) {
    suspend operator fun invoke(userId: String, summary: LaborSummary) {
        laborRepository.saveLaborSummary(userId, summary)
    }
}
