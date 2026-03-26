package com.dadnavigator.app.presentation.screen.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.usecase.checklist.AddChecklistItemUseCase
import com.dadnavigator.app.domain.usecase.checklist.ObserveChecklistsUseCase
import com.dadnavigator.app.domain.usecase.checklist.SeedDefaultChecklistsUseCase
import com.dadnavigator.app.domain.usecase.checklist.ToggleChecklistItemUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for checklist management.
 */
@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val observeChecklistsUseCase: ObserveChecklistsUseCase,
    private val seedDefaultChecklistsUseCase: SeedDefaultChecklistsUseCase,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val toggleChecklistItemUseCase: ToggleChecklistItemUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val draftsState = MutableStateFlow<Map<Long, String>>(emptyMap())
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        combine(
            observeChecklistsUseCase(userId),
            draftsState,
            errorState
        ) { checklists, drafts, errorRes ->
            ChecklistUiState(
                checklists = checklists,
                drafts = drafts,
                errorRes = errorRes
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChecklistUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isBlank() || userIdState.value == userId) return
        userIdState.value = userId
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                seedDefaultChecklistsUseCase(userId)
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun updateDraft(checklistId: Long, value: String) {
        draftsState.update { current -> current + (checklistId to value) }
    }

    fun addItem(checklistId: Long) {
        val userId = userIdState.value
        val draft = draftsState.value[checklistId].orEmpty()
        if (userId.isBlank() || draft.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addChecklistItemUseCase(userId, checklistId, draft)
                draftsState.update { it + (checklistId to "") }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun setItemChecked(itemId: Long, checked: Boolean) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                toggleChecklistItemUseCase(userId, itemId, checked)
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        errorState.value = null
    }
}
