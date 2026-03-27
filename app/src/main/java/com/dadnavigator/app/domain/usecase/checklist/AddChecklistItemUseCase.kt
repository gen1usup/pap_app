package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Adds user-defined checklist item.
 */
class AddChecklistItemUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String, checklistId: Long, text: String) {
        if (text.isBlank()) return
        checklistRepository.addChecklistItem(userId, checklistId, text.trim())
    }
}
