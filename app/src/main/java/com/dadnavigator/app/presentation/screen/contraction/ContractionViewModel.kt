package com.dadnavigator.app.presentation.screen.contraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.service.ActiveLaborRecommendationPolicy
import com.dadnavigator.app.domain.service.LiveContractionSnapshotBuilder
import com.dadnavigator.app.domain.usecase.contraction.DeleteContractionUseCase
import com.dadnavigator.app.domain.usecase.contraction.FinishContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkBirthUseCase
import com.dadnavigator.app.domain.usecase.timeline.ObserveLaborSummaryUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveWaterBreakHistoryUseCase
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
 * ViewModel for the unified active labor screen.
 */
@HiltViewModel
class ContractionViewModel @Inject constructor(
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val finishContractionSessionUseCase: FinishContractionSessionUseCase,
    private val deleteContractionUseCase: DeleteContractionUseCase,
    private val toggleContractionUseCase: ToggleContractionUseCase,
    private val observeWaterBreakHistoryUseCase: ObserveWaterBreakHistoryUseCase,
    private val observeLaborSummaryUseCase: ObserveLaborSummaryUseCase,
    private val liveContractionSnapshotBuilder: LiveContractionSnapshotBuilder,
    private val activeLaborRecommendationPolicy: ActiveLaborRecommendationPolicy,
    private val markBirthUseCase: MarkBirthUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val userIdState = MutableStateFlow("")
    private val infoState = MutableStateFlow<Int?>(null)
    private val errorState = MutableStateFlow<Int?>(null)

    private val ticker: Flow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            delay(1_000)
        }
    }

    val uiState = userIdState
        .flatMapLatest { userId ->
            combine(
                observeContractionStateUseCase(userId),
                observeWaterBreakHistoryUseCase(userId),
                observeLaborSummaryUseCase(userId),
                ticker
            ) { activeState, waterHistory, laborSummary, now ->
                val snapshot = liveContractionSnapshotBuilder.build(activeState, now)
                val latestWaterBreak = waterHistory.maxByOrNull { it.happenedAt }
                val recommendation = activeLaborRecommendationPolicy.resolve(
                    contractionStats = snapshot.stats,
                    latestWaterBreak = latestWaterBreak,
                    birthRecorded = laborSummary.birthTime != null
                )

                ContractionUiState(
                    isSessionActive = snapshot.isSessionActive,
                    sessionId = snapshot.sessionId,
                    activeContractionId = snapshot.activeContractionId,
                    sessionDuration = snapshot.sessionDuration,
                    currentContractionDuration = snapshot.currentContractionDuration,
                    currentInterval = snapshot.currentInterval,
                    stats = snapshot.stats,
                    recommendation = recommendation,
                    latestWaterBreak = latestWaterBreak,
                    waterBreakElapsed = latestWaterBreak?.takeIf { it.isActive }?.let {
                        Duration.between(it.happenedAt, now)
                    },
                    birthRecorded = laborSummary.birthTime != null,
                    contractions = snapshot.contractions
                )
            }.combine(infoState) { partial, infoRes ->
                partial.copy(infoRes = infoRes)
            }.combine(errorState) { partial, errorRes ->
                partial.copy(errorRes = errorRes)
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

    fun finishSession() {
        val sessionId = uiState.value.sessionId ?: return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                finishContractionSessionUseCase(sessionId)
                infoState.value = R.string.saved
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
                toggleContractionUseCase(
                    userId = userId,
                    sessionId = currentState.sessionId,
                    activeContractionId = currentState.activeContractionId
                )
                infoState.value = R.string.saved
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun deleteContraction(contractionId: Long) {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                deleteContractionUseCase(contractionId)
                infoState.value = R.string.contraction_deleted
            }.onFailure {
                errorState.value = R.string.error_generic
            }
        }
    }

    fun markBirthNow(eventTitle: String) {
        val userId = userIdState.value
        if (userId.isBlank()) return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                markBirthUseCase(
                    userId = userId,
                    eventTitle = eventTitle,
                    eventDescription = ""
                )
                infoState.value = R.string.events_birth_saved
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

