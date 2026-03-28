package com.dadnavigator.app.presentation.screen.baby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.timeline.SaveLaborSummaryUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BabyViewModel @Inject constructor(
    private val observeLaborSummaryUseCase: ObserveLaborSummaryUseCase,
    private val saveLaborSummaryUseCase: SaveLaborSummaryUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val formState = MutableStateFlow(BabyFormState())
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        combine(
            observeLaborSummaryUseCase(userId),
            formState,
            infoState,
            errorState
        ) { summary, form, infoRes, errorRes ->
            BabyUiState(
                summary = summary,
                nameInput = form.nameInput.ifBlank { summary.babyName.orEmpty() },
                weightInput = form.weightInput.ifBlank { summary.birthWeightGrams?.toString().orEmpty() },
                heightInput = form.heightInput.ifBlank { summary.birthHeightCm?.toString().orEmpty() },
                infoRes = infoRes,
                errorRes = errorRes
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BabyUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun updateName(value: String) {
        formState.update { it.copy(nameInput = value) }
    }

    fun updateWeight(value: String) {
        formState.update { it.copy(weightInput = value) }
    }

    fun updateHeight(value: String) {
        formState.update { it.copy(heightInput = value) }
    }

    fun save() {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val current = uiState.value.summary
                val form = formState.value
                val weight = form.weightInput.toIntOrNull()
                val height = form.heightInput.toIntOrNull()

                if (form.weightInput.isNotBlank() && weight == null) {
                    errorState.value = R.string.invalid_number
                    return@runCatching
                }
                if (form.heightInput.isNotBlank() && height == null) {
                    errorState.value = R.string.invalid_number
                    return@runCatching
                }

                saveLaborSummaryUseCase(
                    userId = userId,
                    summary = current.copy(
                        babyName = form.nameInput.trim().ifBlank { null },
                        birthWeightGrams = weight,
                        birthHeightCm = height
                    )
                )
                infoState.value = R.string.baby_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun clearMessages() {
        infoState.value = null
        errorState.value = null
    }
}

data class BabyUiState(
    val summary: LaborSummary = LaborSummary(
        laborStartTime = null,
        birthTime = null,
        babyName = null,
        birthWeightGrams = null,
        birthHeightCm = null
    ),
    val nameInput: String = "",
    val weightInput: String = "",
    val heightInput: String = "",
    val infoRes: Int? = null,
    val errorRes: Int? = null
)

private data class BabyFormState(
    val nameInput: String = "",
    val weightInput: String = "",
    val heightInput: String = ""
)
