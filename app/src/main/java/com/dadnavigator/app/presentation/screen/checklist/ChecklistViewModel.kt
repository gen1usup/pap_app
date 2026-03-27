package com.dadnavigator.app.presentation.screen.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.Checklist
import com.dadnavigator.app.domain.usecase.checklist.AddChecklistItemUseCase
import com.dadnavigator.app.domain.usecase.checklist.CreateChecklistUseCase
import com.dadnavigator.app.domain.usecase.checklist.DeleteChecklistItemUseCase
import com.dadnavigator.app.domain.usecase.checklist.ObserveChecklistsUseCase
import com.dadnavigator.app.domain.usecase.checklist.RenameChecklistUseCase
import com.dadnavigator.app.domain.usecase.checklist.SeedDefaultChecklistsUseCase
import com.dadnavigator.app.domain.usecase.checklist.ToggleChecklistItemUseCase
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeChecklistsUseCase: ObserveChecklistsUseCase,
    private val seedDefaultChecklistsUseCase: SeedDefaultChecklistsUseCase,
    private val createChecklistUseCase: CreateChecklistUseCase,
    private val renameChecklistUseCase: RenameChecklistUseCase,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase,
    private val toggleChecklistItemUseCase: ToggleChecklistItemUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val selectedStageState = MutableStateFlow<AppStage?>(null)
    private val itemDraftsState = MutableStateFlow<Map<Long, String>>(emptyMap())
    private val newChecklistTitleState = MutableStateFlow("")
    private val renameTargetState = MutableStateFlow<Checklist?>(null)
    private val renameDraftState = MutableStateFlow("")
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        val checklistBase = combine(
            observeSettingsUseCase(),
            observeChecklistsUseCase(userId),
            selectedStageState,
            itemDraftsState,
            newChecklistTitleState,
        ) { settings, checklists, selectedStage, itemDrafts, newChecklistTitle ->
            val effectiveStage = selectedStage ?: settings.appStage
            ChecklistUiState(
                checklists = checklists.filter { it.checklist.stage == effectiveStage },
                selectedStage = effectiveStage,
                currentStage = settings.appStage,
                itemDrafts = itemDrafts,
                newChecklistTitle = newChecklistTitle
            )
        }

        val dialogState = combine(
            renameTargetState,
            renameDraftState,
            infoState,
            errorState
        ) { renameTarget, renameDraft, infoRes, errorRes ->
            ChecklistUiState(
                renameTarget = renameTarget,
                renameDraft = renameDraft,
                infoRes = infoRes,
                errorRes = errorRes
            )
        }

        combine(checklistBase, dialogState) { base, dialog ->
            base.copy(
                renameTarget = dialog.renameTarget,
                renameDraft = dialog.renameDraft,
                infoRes = dialog.infoRes,
                errorRes = dialog.errorRes
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

    fun selectStage(stage: AppStage) {
        selectedStageState.value = stage
    }

    fun updateNewChecklistTitle(value: String) {
        newChecklistTitleState.value = value
    }

    fun createChecklist() {
        val userId = userIdState.value
        val title = newChecklistTitleState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val createdId = createChecklistUseCase(userId, title, uiState.value.selectedStage)
                if (createdId == null) {
                    errorState.value = R.string.input_required
                } else {
                    newChecklistTitleState.value = ""
                    infoState.value = R.string.saved
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun openRenameDialog(checklist: Checklist) {
        renameTargetState.value = checklist
        renameDraftState.value = checklist.title
    }

    fun updateRenameDraft(value: String) {
        renameDraftState.value = value
    }

    fun renameChecklist() {
        val userId = userIdState.value
        val checklist = renameTargetState.value ?: return
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                renameChecklistUseCase(userId, checklist.id, renameDraftState.value)
                renameTargetState.value = null
                renameDraftState.value = ""
                infoState.value = R.string.saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissRenameDialog() {
        renameTargetState.value = null
        renameDraftState.value = ""
    }

    fun updateItemDraft(checklistId: Long, value: String) {
        itemDraftsState.update { current -> current + (checklistId to value) }
    }

    fun addItem(checklistId: Long) {
        val userId = userIdState.value
        val draft = itemDraftsState.value[checklistId].orEmpty()
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addChecklistItemUseCase(userId, checklistId, draft)
                if (draft.isBlank()) {
                    errorState.value = R.string.input_required
                } else {
                    itemDraftsState.update { it + (checklistId to "") }
                    infoState.value = R.string.saved
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun deleteItem(itemId: Long) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                deleteChecklistItemUseCase(userId, itemId)
                infoState.value = R.string.saved
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

    fun dismissMessages() {
        infoState.value = null
        errorState.value = null
    }
}
