package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.repository.ChecklistRepository
import javax.inject.Inject

/**
 * Creates a user-managed checklist for the chosen app stage.
 */
class CreateChecklistUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    suspend operator fun invoke(userId: String, title: String, stage: AppStage): Long? {
        if (title.isBlank()) return null
        return checklistRepository.createChecklist(userId, title.trim(), stage)
    }
}
