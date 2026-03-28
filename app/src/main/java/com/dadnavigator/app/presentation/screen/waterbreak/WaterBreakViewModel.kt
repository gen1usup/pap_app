package com.dadnavigator.app.presentation.screen.waterbreak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.AddWaterBreakEventUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.CloseWaterBreakEventUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.DeleteWaterBreakEventUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveWaterBreakHistoryUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for water break tracking.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WaterBreakViewModel @Inject constructor(
    private val observeActiveWaterBreakUseCase: ObserveActiveWaterBreakUseCase,
    private val observeWaterBreakHistoryUseCase: ObserveWaterBreakHistoryUseCase,
    private val addWaterBreakEventUseCase: AddWaterBreakEventUseCase,
    private val closeWaterBreakEventUseCase: CloseWaterBreakEventUseCase,
    private val deleteWaterBreakEventUseCase: DeleteWaterBreakEventUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val selectedColorState = MutableStateFlow(WaterColor.CLEAR)
    private val notesState = MutableStateFlow("")
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    private val ticker = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    val uiState = userIdState.flatMapLatest { userId ->
        combine(
            observeActiveWaterBreakUseCase(userId),
            observeWaterBreakHistoryUseCase(userId),
            selectedColorState,
            notesState,
            ticker
        ) { activeEvent, history, selectedColor, notes, now ->
            WaterBreakPartialState(
                activeEvent = activeEvent,
                history = history,
                selectedColor = selectedColor,
                notes = notes,
                now = now
            )
        }.combine(infoState) { partial, infoRes ->
            partial to infoRes
        }.combine(errorState) { (partial, infoRes), errorRes ->
            WaterBreakUiState(
                activeEvent = partial.activeEvent,
                history = partial.history,
                elapsed = partial.activeEvent?.let {
                    Duration.between(it.happenedAt, partial.now)
                } ?: Duration.ZERO,
                selectedColor = partial.selectedColor,
                notes = partial.notes,
                infoRes = infoRes,
                errorRes = errorRes
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WaterBreakUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun setColor(color: WaterColor) {
        selectedColorState.value = color
    }

    fun setNotes(notes: String) {
        notesState.value = notes
    }

    fun saveEvent() {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val now = Instant.now()
                addWaterBreakEventUseCase(
                    userId = userId,
                    happenedAt = now,
                    color = selectedColorState.value,
                    notes = notesState.value
                )
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = now,
                    title = "",
                    description = notesState.value,
                    type = TimelineType.WATER_BREAK,
                    stageAtCreation = AppStage.LABOR
                )
                notesState.value = ""
                infoState.value = R.string.timeline_event_saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun closeActiveEvent() {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                closeWaterBreakEventUseCase(userId = userId)
                infoState.value = R.string.saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun deleteEvent(eventId: Long) {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                deleteWaterBreakEventUseCase(eventId)
                infoState.value = R.string.water_break_deleted
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        infoState.value = null
        errorState.value = null
    }
}

private data class WaterBreakPartialState(
    val activeEvent: WaterBreakEvent?,
    val history: List<WaterBreakEvent>,
    val selectedColor: WaterColor,
    val notes: String,
    val now: Instant
)
