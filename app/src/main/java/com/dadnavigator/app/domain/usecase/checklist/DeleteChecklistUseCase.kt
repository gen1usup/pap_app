package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Removes a user-created checklist and its items.
 */
class DeleteChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String, checklistId: Long) {
        checklistRepository.deleteChecklist(userId, checklistId)
    }
}
