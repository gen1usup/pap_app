package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Ensures base system checklists exist.
 */
class SeedDefaultChecklistsUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String) {
        checklistRepository.seedDefaultChecklistsIfNeeded(userId)
    }
}
