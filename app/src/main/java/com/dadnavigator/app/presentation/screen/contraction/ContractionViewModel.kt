package com.dadnavigator.app.presentation.screen.contraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.contraction.CalculateContractionStatsUseCase
import com.dadnavigator.app.domain.usecase.contraction.FinishContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.contraction.FinishContractionUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionUseCase
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for contraction tracker and analytics.
 */
@HiltViewModel
class ContractionViewModel @Inject constructor(
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val startContractionSessionUseCase: StartContractionSessionUseCase,
    private val finishContractionSessionUseCase: FinishContractionSessionUseCase,
    private val startContractionUseCase: StartContractionUseCase,
    private val finishContractionUseCase: FinishContractionUseCase,
    private val calculateContractionStatsUseCase: CalculateContractionStatsUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val errorState = MutableStateFlow<Int?>(null)

    private val ticker: Flow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    val uiState = userIdState
        .flatMapLatest { userId ->
            combine(observeContractionStateUseCase(userId), ticker, errorState) { activeState, now, errorRes ->
                val stats = calculateContractionStatsUseCase(activeState.contractions)
                val sessionDuration = activeState.session?.let { Duration.between(it.startedAt, now) } ?: Duration.ZERO
                val currentDuration = activeState.activeContraction?.let {
                    Duration.between(it.startedAt, now)
                } ?: Duration.ZERO
                ContractionUiState(
                    isSessionActive = activeState.session?.isActive == true,
                    sessionId = activeState.session?.id,
                    activeContractionId = activeState.activeContraction?.id,
                    sessionDuration = sessionDuration,
                    currentContractionDuration = currentDuration,
                    stats = stats,
                    contractions = activeState.contractions,
                    errorRes = errorRes
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ContractionUiState()
        )

    fun setUserId(userId: String) {
        if (userId.isNotBlank() && userIdState.value != userId) {
            userIdState.value = userId
        }
    }

    fun startSession() {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                startContractionSessionUseCase(userId)
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun finishSession() {
        val sessionId = uiState.value.sessionId ?: return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                finishContractionSessionUseCase(sessionId)
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun startOrFinishContraction() {
        val currentState = uiState.value
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val sessionId = currentState.sessionId ?: return@runCatching
                val activeContractionId = currentState.activeContractionId
                if (activeContractionId == null) {
                    startContractionUseCase(sessionId = sessionId, userId = userId)
                } else {
                    finishContractionUseCase(activeContractionId)
                    addTimelineEventUseCase(
                        userId = userId,
                        timestamp = Instant.now(),
                        title = "",
                        description = "",
                        type = TimelineType.CONTRACTION
                    )
                }
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun dismissError() {
        errorState.value = null
    }

    fun completedContractions(): List<Contraction> {
        return uiState.value.contractions.filter { it.endedAt != null }
    }
}
