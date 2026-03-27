package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Removes one checklist item from local storage.
 */
class DeleteChecklistItemUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String, itemId: Long) {
        checklistRepository.deleteItem(userId, itemId)
    }
}
