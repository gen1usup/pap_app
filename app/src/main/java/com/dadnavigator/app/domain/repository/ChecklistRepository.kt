package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.ChecklistWithItems
import kotlinx.coroutines.flow.Flow

/**
 * Contract for checklist management.
 */
interface ChecklistRepository {
    fun observeChecklists(userId: String): Flow<List<ChecklistWithItems>>

    suspend fun seedDefaultChecklistsIfNeeded(userId: String)

    suspend fun addCustomItem(userId: String, checklistId: Long, text: String)

    suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean)
}
