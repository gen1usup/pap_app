package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.ChecklistDao
import com.dadnavigator.app.data.local.entity.ChecklistEntity
import com.dadnavigator.app.data.local.entity.ChecklistItemEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Room-backed implementation for checklist feature with default seeded content.
 */
class ChecklistRepositoryImpl @Inject constructor(
    private val checklistDao: ChecklistDao
) : ChecklistRepository {

    override fun observeChecklists(userId: String): Flow<List<ChecklistWithItems>> {
        return checklistDao.observeChecklists(userId).map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override suspend fun seedDefaultChecklistsIfNeeded(userId: String) {
        if (checklistDao.countChecklists(userId) > 0) return

        defaultChecklistTemplates.forEach { template ->
            val checklistId = checklistDao.insertChecklist(
                ChecklistEntity(
                    userId = userId,
                    title = template.title,
                    stage = template.stage.name,
                    category = template.category,
                    isSystem = true,
                    sortOrder = template.sortOrder,
                    createdAt = Instant.now()
                )
            )
            val items = template.items.map { itemText ->
                ChecklistItemEntity(
                    checklistId = checklistId,
                    userId = userId,
                    text = itemText,
                    isChecked = false,
                    createdAt = Instant.now()
                )
            }
            checklistDao.insertItems(items)
        }
    }

    override suspend fun addCustomItem(userId: String, checklistId: Long, text: String) {
        checklistDao.insertItem(
            ChecklistItemEntity(
                checklistId = checklistId,
                userId = userId,
                text = text,
                isChecked = false,
                createdAt = Instant.now()
            )
        )
    }

    override suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean) {
        checklistDao.setItemChecked(userId, itemId, checked)
    }
}
