package com.dadnavigator.app.presentation.screen.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveTimelineUseCase
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

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val observeTimelineUseCase: ObserveTimelineUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val filterState = MutableStateFlow(TimelineFilter.ALL)
    private val expandedEventIdsState = MutableStateFlow<Set<Long>>(emptySet())
    private val formState = MutableStateFlow(TimelineFormState())
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    val uiState = userIdState.flatMapLatest { userId ->
        val timelineBase = combine(
            observeTimelineUseCase(userId),
            filterState,
            expandedEventIdsState,
            formState
        ) { events, filter, expandedEventIds, form ->
            TimelineUiState(
                filter = filter,
                events = when (filter) {
                    TimelineFilter.ALL -> events
                    TimelineFilter.LABOR -> events.filter {
                        it.type == TimelineType.LABOR ||
                            it.type == TimelineType.BIRTH ||
                            it.type == TimelineType.CONTRACTION ||
                            it.type == TimelineType.WATER_BREAK ||
                            it.type == TimelineType.LABOR_NOTE ||
                            it.type == TimelineType.HOSPITAL_NOTE ||
                            it.type == TimelineType.NOTE
                    }
                    TimelineFilter.POSTPARTUM -> events.filter {
                        it.type == TimelineType.FEEDING ||
                            it.type == TimelineType.DIAPER ||
                            it.type == TimelineType.SLEEP ||
                            it.type == TimelineType.HOME_NOTE ||
                            it.type == TimelineType.NOTE
                    }
                    TimelineFilter.NOTES -> events.filter {
                        it.type == TimelineType.NOTE ||
                            it.type == TimelineType.PREPARATION_NOTE ||
                            it.type == TimelineType.LABOR_NOTE ||
                            it.type == TimelineType.HOSPITAL_NOTE ||
                            it.type == TimelineType.HOME_NOTE
                    }
                },
                expandedEventIds = expandedEventIds,
                showAddSheet = form.showAddSheet,
                selectedType = form.selectedType,
                draftTitle = form.title,
                draftDescription = form.description
            )
        }
        timelineBase
            .combine(infoState) { partial, infoRes ->
                partial.copy(infoRes = infoRes)
            }
            .combine(errorState) { partial, errorRes ->
                partial.copy(errorRes = errorRes)
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimelineUiState()
    )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun setFilter(filter: TimelineFilter) {
        filterState.value = filter
    }

    fun toggleExpanded(eventId: Long) {
        expandedEventIdsState.update { current ->
            if (eventId in current) current - eventId else current + eventId
        }
    }

    fun showAddSheet() {
        formState.update { it.copy(showAddSheet = true) }
    }

    fun hideAddSheet() {
        formState.update { it.copy(showAddSheet = false) }
    }

    fun updateType(type: TimelineType) {
        formState.update { it.copy(selectedType = type) }
    }

    fun updateTitle(value: String) {
        formState.update { it.copy(title = value) }
    }

    fun updateDescription(value: String) {
        formState.update { it.copy(description = value) }
    }

    fun saveEvent() {
        val userId = userIdState.value
        val form = formState.value
        if (userId.isBlank()) return
        if (form.title.isBlank() && form.selectedType == TimelineType.LABOR) {
            errorState.value = R.string.input_required
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                addTimelineEventUseCase(
                    userId = userId,
                    timestamp = Instant.now(),
                    title = form.title,
                    description = form.description,
                    type = form.selectedType
                )
                formState.value = TimelineFormState(selectedType = form.selectedType)
                infoState.value = R.string.timeline_event_saved
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

private data class TimelineFormState(
    val showAddSheet: Boolean = false,
    val selectedType: TimelineType = TimelineType.LABOR,
    val title: String = "",
    val description: String = ""
)
