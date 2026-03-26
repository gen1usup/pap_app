package com.dadnavigator.app.data.repository

import android.content.Context
import com.dadnavigator.app.R
import com.dadnavigator.app.data.local.dao.ChecklistDao
import com.dadnavigator.app.data.local.entity.ChecklistEntity
import com.dadnavigator.app.data.local.entity.ChecklistItemEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.domain.repository.ChecklistRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/**
 * Room-backed implementation for checklist feature with default seeded content.
 */
class ChecklistRepositoryImpl @Inject constructor(
    private val checklistDao: ChecklistDao,
    @ApplicationContext private val context: Context
) : ChecklistRepository {

    override fun observeChecklists(userId: String): Flow<List<ChecklistWithItems>> {
        return checklistDao.observeChecklists(userId).map { relations ->
            relations.map { it.toDomain() }
        }
    }

    override suspend fun seedDefaultChecklistsIfNeeded(userId: String) {
        if (checklistDao.countChecklists(userId) > 0) return

        val names = context.resources.getStringArray(R.array.checklist_names)
        val itemArrays = listOf(
            R.array.checklist_bag_items,
            R.array.checklist_documents_items,
            R.array.checklist_before_departure_items,
            R.array.checklist_postpartum_items
        )

        names.forEachIndexed { index, name ->
            val checklistId = checklistDao.insertChecklist(
                ChecklistEntity(
                    userId = userId,
                    name = name,
                    isSystem = true,
                    createdAt = Instant.now()
                )
            )
            val items = context.resources.getStringArray(itemArrays[index]).map { itemText ->
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
