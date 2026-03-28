package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.ChecklistDao
import com.dadnavigator.app.data.local.entity.ChecklistEntity
import com.dadnavigator.app.data.local.entity.ChecklistItemEntity
import com.dadnavigator.app.data.local.entity.ChecklistWithItemsRelation
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistRepositoryImplTest {

    @Test
    fun `deleted default checklist is not restored by repeated seeding`() = runBlocking {
        val dao = FakeChecklistDao()
        val repository = ChecklistRepositoryImpl(dao)
        val userId = "user"

        repository.seedDefaultChecklistsIfNeeded(userId)
        val seededIds = dao.activeChecklistIds().toList()
        assertTrue(seededIds.isNotEmpty())

        seededIds.forEach { checklistId ->
            repository.deleteChecklist(userId, checklistId)
        }
        assertEquals(0, dao.countActiveChecklists(userId))
        assertEquals(seededIds.size, dao.countAllChecklists(userId))

        repository.seedDefaultChecklistsIfNeeded(userId)

        assertEquals(0, dao.countActiveChecklists(userId))
        assertEquals(seededIds.size, dao.countAllChecklists(userId))
    }
}

private class FakeChecklistDao : ChecklistDao {
    private val checklists = mutableListOf<ChecklistEntity>()
    private val items = mutableListOf<ChecklistItemEntity>()
    private var nextChecklistId = 1L
    private var nextItemId = 1L

    fun activeChecklistIds(): List<Long> = checklists.filterNot { it.isDeleted }.map { it.id }

    override fun observeChecklists(userId: String): Flow<List<ChecklistWithItemsRelation>> = emptyFlow()

    override suspend fun insertChecklist(entity: ChecklistEntity): Long {
        val assigned = entity.copy(id = nextChecklistId++)
        checklists += assigned
        return assigned.id
    }

    override suspend fun insertItems(entities: List<ChecklistItemEntity>) {
        entities.forEach { insertItem(it) }
    }

    override suspend fun insertItem(entity: ChecklistItemEntity) {
        items += entity.copy(id = nextItemId++)
    }

    override suspend fun renameChecklist(userId: String, checklistId: Long, title: String) {
        val index = checklists.indexOfFirst { it.id == checklistId && it.userId == userId && !it.isDeleted }
        if (index >= 0) {
            checklists[index] = checklists[index].copy(title = title)
        }
    }

    override suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean) {
        val index = items.indexOfFirst { it.id == itemId && it.userId == userId }
        if (index >= 0) {
            items[index] = items[index].copy(isChecked = checked)
        }
    }

    override suspend fun deleteItem(userId: String, itemId: Long) {
        items.removeAll { it.id == itemId && it.userId == userId }
    }

    override suspend fun deleteItemsByChecklist(userId: String, checklistId: Long) {
        items.removeAll { it.userId == userId && it.checklistId == checklistId }
    }

    override suspend fun softDeleteChecklist(userId: String, checklistId: Long) {
        val index = checklists.indexOfFirst { it.id == checklistId && it.userId == userId }
        if (index >= 0) {
            checklists[index] = checklists[index].copy(isDeleted = true)
        }
    }

    override suspend fun countActiveChecklists(userId: String): Int =
        checklists.count { it.userId == userId && !it.isDeleted }

    override suspend fun countAllChecklists(userId: String): Int =
        checklists.count { it.userId == userId }

    override suspend fun clearChecklists() {
        checklists.clear()
    }

    override suspend fun clearItems() {
        items.clear()
    }
}
