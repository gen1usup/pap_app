package com.dadnavigator.app.domain.usecase.checklist

import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams checklist data for UI.
 */
class ObserveChecklistsUseCase @Inject constructor(
    private val checklistRepository: ChecklistRepository
) {
    operator fun invoke(userId: String): Flow<List<ChecklistWithItems>> =
        checklistRepository.observeChecklists(userId)
}
