package com.dadnavigator.app.presentation.screen.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveTimelineUseCase
import com.dadnavigator.app.domain.usecase.timeline.SaveLaborSummaryUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
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
 * ViewModel for labor phase events and summary.
 */
@HiltViewModel
class LaborViewModel @Inject constructor(
    private val observeLaborSummaryUseCase: ObserveLaborSummaryUseCase,
    private val saveLaborSummaryUseCase: SaveLaborSummaryUseCase,
    private val observeTimelineUseCase: ObserveTimelineUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val formState = MutableStateFlow(LaborFormState())
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        combine(
            observeLaborSummaryUseCase(userId),
            observeTimelineUseCase(userId),
            formState,
            errorState
        ) { summary, timeline, form, errorRes ->
            LaborUiState(
                summary = summary,
                laborEvents = timeline.filter { it.type == TimelineType.LABOR || it.type == TimelineType.BIRTH },
                weightInput = form.weightInput,
                heightInput = form.heightInput,
                customEventTitle = form.customEventTitle,
                customEventNote = form.customEventNote,
                errorRes = errorRes
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LaborUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun markLaborStartNow() {
        formState.update { it.copy(laborStartTime = Instant.now()) }
    }

    fun markBirthNow() {
        formState.update { it.copy(birthTime = Instant.now()) }
    }

    fun updateWeight(value: String) {
        formState.update { it.copy(weightInput = value) }
    }

    fun updateHeight(value: String) {
        formState.update { it.copy(heightInput = value) }
    }

    fun updateEventTitle(value: String) {
        formState.update { it.copy(customEventTitle = value) }
    }

    fun updateEventNote(value: String) {
        formState.update { it.copy(customEventNote = value) }
    }

    fun saveSummary() {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val currentSummary = uiState.value.summary
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

                val summary = LaborSummary(
                    laborStartTime = form.laborStartTime ?: currentSummary.laborStartTime,
                    birthTime = form.birthTime ?: currentSummary.birthTime,
                    birthWeightGrams = weight ?: currentSummary.birthWeightGrams,
                    birthHeightCm = height ?: currentSummary.birthHeightCm
                )
                saveLaborSummaryUseCase(userId, summary)

                if (currentSummary.birthTime == null && summary.birthTime != null) {
                    addTimelineEventUseCase(
                        userId = userId,
                        timestamp = summary.birthTime,
                        title = "",
                        description = "",
                        type = TimelineType.BIRTH
                    )
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun addCustomEvent() {
        val userId = userIdState.value
        val form = formState.value
        if (userId.isBlank() || form.customEventTitle.isBlank()) {
            errorState.value = R.string.input_required
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = Instant.now(),
                    title = form.customEventTitle.trim(),
                    description = form.customEventNote.trim(),
                    type = TimelineType.LABOR
                )
                formState.update { it.copy(customEventTitle = "", customEventNote = "") }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        errorState.value = null
    }
}

private data class LaborFormState(
    val laborStartTime: Instant? = null,
    val birthTime: Instant? = null,
    val weightInput: String = "",
    val heightInput: String = "",
    val customEventTitle: String = "",
    val customEventNote: String = ""
)
