package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Updates title of a custom checklist.
 */
class RenameChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String, checklistId: Long, title: String) {
        if (title.isBlank()) return
        checklistRepository.renameChecklist(userId, checklistId, title.trim())
    }
}
