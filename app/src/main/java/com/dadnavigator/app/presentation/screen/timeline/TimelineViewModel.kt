package com.dadnavigator.app.presentation.screen.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.timeline.ObserveTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for timeline rendering and filtering.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val observeTimelineUseCase: ObserveTimelineUseCase
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val filterState = MutableStateFlow(TimelineFilter.ALL)

    val uiState = userIdState.flatMapLatest { userId ->
        combine(observeTimelineUseCase(userId), filterState) { events, filter ->
            TimelineUiState(
                filter = filter,
                events = when (filter) {
                    TimelineFilter.ALL -> events
                    TimelineFilter.LABOR -> events.filter {
                        it.type == TimelineType.LABOR ||
                            it.type == TimelineType.BIRTH ||
                            it.type == TimelineType.CONTRACTION ||
                            it.type == TimelineType.WATER_BREAK
                    }

                    TimelineFilter.POSTPARTUM -> events.filter {
                        it.type == TimelineType.FEEDING ||
                            it.type == TimelineType.DIAPER ||
                            it.type == TimelineType.SLEEP ||
                            it.type == TimelineType.NOTE
                    }
                }
            )
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
}
