package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ChecklistWithItems
import kotlinx.coroutines.flow.Flow

/**
 * Contract for checklist management.
 */
interface ChecklistRepository {
    fun observeChecklists(userId: String): Flow<List<ChecklistWithItems>>

    suspend fun seedDefaultChecklistsIfNeeded(userId: String)

    suspend fun createChecklist(
        userId: String,
        title: String,
        stage: AppStage
    ): Long

    suspend fun renameChecklist(
        userId: String,
        checklistId: Long,
        title: String
    )

    suspend fun deleteChecklist(userId: String, checklistId: Long)

    suspend fun addChecklistItem(userId: String, checklistId: Long, text: String)

    suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean)

    suspend fun deleteItem(userId: String, itemId: Long)
}
