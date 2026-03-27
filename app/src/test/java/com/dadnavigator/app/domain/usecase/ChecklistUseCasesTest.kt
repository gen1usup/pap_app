package com.dadnavigator.app.domain.usecase

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.domain.repository.ChecklistRepository
import com.dadnavigator.app.domain.usecase.checklist.AddChecklistItemUseCase
import com.dadnavigator.app.domain.usecase.checklist.CreateChecklistUseCase
import com.dadnavigator.app.domain.usecase.checklist.DeleteChecklistItemUseCase
import com.dadnavigator.app.domain.usecase.checklist.DeleteChecklistUseCase
import com.dadnavigator.app.domain.usecase.checklist.RenameChecklistUseCase
import com.dadnavigator.app.domain.usecase.checklist.ToggleChecklistItemUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for checklist use cases.
 */
class ChecklistUseCasesTest {

    private val fakeRepository = FakeChecklistRepository()
    private val createChecklistUseCase = CreateChecklistUseCase(fakeRepository)
    private val renameChecklistUseCase = RenameChecklistUseCase(fakeRepository)
    private val deleteChecklistUseCase = DeleteChecklistUseCase(fakeRepository)
    private val addChecklistItemUseCase = AddChecklistItemUseCase(fakeRepository)
    private val deleteChecklistItemUseCase = DeleteChecklistItemUseCase(fakeRepository)
    private val toggleChecklistItemUseCase = ToggleChecklistItemUseCase(fakeRepository)

    @Test
    fun `does not create blank checklist`() = runBlocking {
        val createdId = createChecklistUseCase("user", "   ", AppStage.PREPARING)

        assertNull(createdId)
        assertNull(fakeRepository.createdChecklistTitle)
    }

    @Test
    fun `creates trimmed checklist for selected stage`() = runBlocking {
        val createdId = createChecklistUseCase("user", "  Документы в машину  ", AppStage.CONTRACTIONS)

        assertEquals(77L, createdId)
        assertEquals("Документы в машину", fakeRepository.createdChecklistTitle)
        assertEquals(AppStage.CONTRACTIONS, fakeRepository.createdChecklistStage)
    }

    @Test
    fun `does not add blank custom item`() = runBlocking {
        addChecklistItemUseCase("user", 1L, "   ")

        assertNull(fakeRepository.lastAddedText)
    }

    @Test
    fun `adds trimmed custom item`() = runBlocking {
        addChecklistItemUseCase("user", 42L, "  Паспорт  ")

        assertEquals("Паспорт", fakeRepository.lastAddedText)
        assertEquals(42L, fakeRepository.lastChecklistId)
    }

    @Test
    fun `renames checklist with trimmed title`() = runBlocking {
        renameChecklistUseCase("user", 42L, "  Перед выездом  ")

        assertEquals(42L, fakeRepository.lastRenamedChecklistId)
        assertEquals("Перед выездом", fakeRepository.lastRenamedTitle)
    }

    @Test
    fun `delegates toggle operation`() = runBlocking {
        toggleChecklistItemUseCase("user", itemId = 5L, checked = true)

        assertEquals(5L, fakeRepository.lastToggledItemId)
        assertEquals(true, fakeRepository.lastChecked)
    }

    @Test
    fun `delegates delete item and delete checklist`() = runBlocking {
        deleteChecklistItemUseCase("user", itemId = 6L)
        deleteChecklistUseCase("user", checklistId = 9L)

        assertEquals(6L, fakeRepository.lastDeletedItemId)
        assertEquals(9L, fakeRepository.lastDeletedChecklistId)
    }
}

private class FakeChecklistRepository : ChecklistRepository {
    var createdChecklistTitle: String? = null
    var createdChecklistStage: AppStage? = null
    var lastAddedText: String? = null
    var lastChecklistId: Long? = null
    var lastRenamedChecklistId: Long? = null
    var lastRenamedTitle: String? = null
    var lastDeletedChecklistId: Long? = null
    var lastDeletedItemId: Long? = null
    var lastToggledItemId: Long? = null
    var lastChecked: Boolean? = null

    override fun observeChecklists(userId: String): Flow<List<ChecklistWithItems>> = emptyFlow()

    override suspend fun seedDefaultChecklistsIfNeeded(userId: String) = Unit

    override suspend fun createChecklist(userId: String, title: String, stage: AppStage): Long {
        createdChecklistTitle = title
        createdChecklistStage = stage
        return 77L
    }

    override suspend fun renameChecklist(userId: String, checklistId: Long, title: String) {
        lastRenamedChecklistId = checklistId
        lastRenamedTitle = title
    }

    override suspend fun deleteChecklist(userId: String, checklistId: Long) {
        lastDeletedChecklistId = checklistId
    }

    override suspend fun addChecklistItem(userId: String, checklistId: Long, text: String) {
        lastChecklistId = checklistId
        lastAddedText = text
    }

    override suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean) {
        lastToggledItemId = itemId
        lastChecked = checked
    }

    override suspend fun deleteItem(userId: String, itemId: Long) {
        lastDeletedItemId = itemId
    }
}
