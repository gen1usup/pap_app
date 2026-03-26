package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Updates completion state of one checklist item.
 */
class ToggleChecklistItemUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String, itemId: Long, checked: Boolean) {
        checklistRepository.setItemChecked(userId, itemId, checked)
    }
}
