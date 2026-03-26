package com.dadnavigator.app.presentation.screen.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.ChecklistWithItems
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.usecase.checklist.ObserveChecklistsUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveTimelineUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val observeActiveWaterBreakUseCase: ObserveActiveWaterBreakUseCase,
    private val observeChecklistsUseCase: ObserveChecklistsUseCase,
    private val observeTimelineUseCase: ObserveTimelineUseCase,
    private val startContractionSessionUseCase: StartContractionSessionUseCase,
    private val startContractionUseCase: StartContractionUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow(DEFAULT_USER_ID)
    private val errorState = MutableStateFlow<Int?>(null)

    private val ticker = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    val uiState = userIdState
        .flatMapLatest { userId ->
            combine(
                observeContractionStateUseCase(userId),
                observeActiveWaterBreakUseCase(userId),
                observeChecklistsUseCase(userId),
                observeTimelineUseCase(userId),
                ticker
            ) { contractionState, waterBreakEvent, checklists, timeline, now ->
                val totals = checklistTotals(checklists)
                DashboardUiPartialState(
                    hasActiveContractionSession = contractionState.session?.isActive == true,
                    hasActiveWaterBreak = waterBreakEvent?.isActive == true,
                    waterBreakElapsed = waterBreakEvent?.elapsed(now),
                    currentActionRes = when {
                        waterBreakEvent?.isActive == true -> R.string.now_action_water_break
                        contractionState.session?.isActive == true -> R.string.now_action_contraction
                        else -> R.string.now_action_default
                    },
                    checklistCompletedCount = totals.first,
                    checklistTotalCount = totals.second,
                    recentEvents = timeline.take(4)
                )
            }.combine(errorState) { partial, errorRes ->
                DashboardUiState(
                    hasActiveContractionSession = partial.hasActiveContractionSession,
                    hasActiveWaterBreak = partial.hasActiveWaterBreak,
                    waterBreakElapsed = partial.waterBreakElapsed,
                    currentActionRes = partial.currentActionRes,
                    checklistCompletedCount = partial.checklistCompletedCount,
                    checklistTotalCount = partial.checklistTotalCount,
                    recentEvents = partial.recentEvents,
                    errorRes = errorRes
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun startFirstContraction() {
        val userId = userIdState.value
        if (userId.isBlank() || uiState.value.hasActiveContractionSession) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val sessionId = startContractionSessionUseCase(userId)
                startContractionUseCase(sessionId = sessionId, userId = userId)
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        errorState.value = null
    }
}

private fun checklistTotals(checklists: List<ChecklistWithItems>): Pair<Int, Int> {
    val completed = checklists.sumOf { it.completedCount }
    val total = checklists.sumOf { it.totalCount }
    return completed to total
}

data class DashboardUiState(
    val hasActiveContractionSession: Boolean = false,
    val hasActiveWaterBreak: Boolean = false,
    val waterBreakElapsed: Duration? = null,
    @StringRes val currentActionRes: Int = R.string.now_action_default,
    val checklistCompletedCount: Int = 0,
    val checklistTotalCount: Int = 0,
    val recentEvents: List<TimelineEvent> = emptyList(),
    val errorRes: Int? = null
)

private data class DashboardUiPartialState(
    val hasActiveContractionSession: Boolean,
    val hasActiveWaterBreak: Boolean,
    val waterBreakElapsed: Duration?,
    @StringRes val currentActionRes: Int,
    val checklistCompletedCount: Int,
    val checklistTotalCount: Int,
    val recentEvents: List<TimelineEvent>
)
