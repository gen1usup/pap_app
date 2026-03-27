package com.dadnavigator.app.presentation.screen.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkBirthUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkLaborStartedUseCase
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
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
 * Coordinates high-priority labor and birth actions from one contextual screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val observeActiveWaterBreakUseCase: ObserveActiveWaterBreakUseCase,
    private val observeLaborSummaryUseCase: ObserveLaborSummaryUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    private val markLaborStartedUseCase: MarkLaborStartedUseCase,
    private val markBirthUseCase: MarkBirthUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val formState = MutableStateFlow(BirthFormState())
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        combine(
            observeSettingsUseCase(),
            observeContractionStateUseCase(userId),
            observeActiveWaterBreakUseCase(userId),
            observeLaborSummaryUseCase(userId),
            formState
        ) { settings, contractionState, waterBreak, laborSummary, form ->
            EventsUiState(
                appStage = settings.appStage,
                hasActiveContractionSession = contractionState.session?.isActive == true,
                hasActiveWaterBreak = waterBreak?.isActive == true,
                laborSummary = laborSummary,
                showBirthSheet = form.showBirthSheet,
                babyNameInput = form.babyName,
                weightInput = form.weightInput,
                heightInput = form.heightInput,
                infoRes = null,
                errorRes = null
            )
        }.combine(infoState) { partial, infoRes ->
            partial.copy(infoRes = infoRes)
        }.combine(errorState) { partial, errorRes ->
            partial.copy(errorRes = errorRes)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EventsUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun markLaborStarted(eventTitle: String, eventDescription: String) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                markLaborStartedUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = eventDescription
                )
                infoState.value = R.string.events_labor_started_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun addJourneyEvent(title: String, description: String = "") {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = Instant.now(),
                    title = title,
                    description = description,
                    type = TimelineType.LABOR
                )
                infoState.value = R.string.timeline_event_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun showBirthSheet(summary: LaborSummary) {
        formState.value = BirthFormState(
            showBirthSheet = true,
            babyName = summary.babyName.orEmpty(),
            weightInput = summary.birthWeightGrams?.toString().orEmpty(),
            heightInput = summary.birthHeightCm?.toString().orEmpty()
        )
    }

    fun hideBirthSheet() {
        formState.update { it.copy(showBirthSheet = false) }
    }

    fun updateBabyName(value: String) {
        formState.update { it.copy(babyName = value) }
    }

    fun updateWeight(value: String) {
        formState.update { it.copy(weightInput = value) }
    }

    fun updateHeight(value: String) {
        formState.update { it.copy(heightInput = value) }
    }

    fun markBirth(eventTitle: String) {
        markBirthInternal(
            eventTitle = eventTitle,
            includeOptionalDetails = true
        )
    }

    fun markBirthWithoutDetails(eventTitle: String) {
        markBirthInternal(
            eventTitle = eventTitle,
            includeOptionalDetails = false
        )
    }

    private fun markBirthInternal(
        eventTitle: String,
        includeOptionalDetails: Boolean
    ) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        val form = formState.value
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val weight = if (includeOptionalDetails && form.weightInput.isNotBlank()) {
                    form.weightInput.toIntOrNull()
                } else {
                    null
                }
                val height = if (includeOptionalDetails && form.heightInput.isNotBlank()) {
                    form.heightInput.toIntOrNull()
                } else {
                    null
                }

                if (includeOptionalDetails && form.weightInput.isNotBlank() && weight == null) {
                    errorState.value = R.string.invalid_number
                    return@runCatching
                }
                if (includeOptionalDetails && form.heightInput.isNotBlank() && height == null) {
                    errorState.value = R.string.invalid_number
                    return@runCatching
                }

                val description = buildString {
                    if (includeOptionalDetails && form.babyName.isNotBlank()) {
                        append("Имя: ")
                        append(form.babyName.trim())
                    }
                    if (includeOptionalDetails && weight != null) {
                        if (isNotBlank()) append(" • ")
                        append("Вес: ")
                        append(weight)
                        append(" г")
                    }
                    if (includeOptionalDetails && height != null) {
                        if (isNotBlank()) append(" • ")
                        append("Рост: ")
                        append(height)
                        append(" см")
                    }
                }

                markBirthUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = description,
                    babyName = form.babyName.takeIf { includeOptionalDetails },
                    birthWeightGrams = weight,
                    birthHeightCm = height
                )
                formState.value = BirthFormState()
                infoState.value = R.string.events_birth_saved
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

private data class BirthFormState(
    val showBirthSheet: Boolean = false,
    val babyName: String = "",
    val weightInput: String = "",
    val heightInput: String = ""
)
