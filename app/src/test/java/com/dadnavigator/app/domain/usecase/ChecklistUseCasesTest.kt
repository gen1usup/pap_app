package com.dadnavigator.app.domain.usecase

import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.domain.repository.ChecklistRepository
import com.dadnavigator.app.domain.usecase.checklist.AddChecklistItemUseCase
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
    private val addChecklistItemUseCase = AddChecklistItemUseCase(fakeRepository)
    private val toggleChecklistItemUseCase = ToggleChecklistItemUseCase(fakeRepository)

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
    fun `delegates toggle operation`() = runBlocking {
        toggleChecklistItemUseCase("user", itemId = 5L, checked = true)

        assertEquals(5L, fakeRepository.lastToggledItemId)
        assertEquals(true, fakeRepository.lastChecked)
    }
}

private class FakeChecklistRepository : ChecklistRepository {
    var lastAddedText: String? = null
    var lastChecklistId: Long? = null
    var lastToggledItemId: Long? = null
    var lastChecked: Boolean? = null

    override fun observeChecklists(userId: String): Flow<List<ChecklistWithItems>> = emptyFlow()

    override suspend fun seedDefaultChecklistsIfNeeded(userId: String) = Unit

    override suspend fun addCustomItem(userId: String, checklistId: Long, text: String) {
        lastChecklistId = checklistId
        lastAddedText = text
    }

    override suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean) {
        lastToggledItemId = itemId
        lastChecked = checked
    }
}
