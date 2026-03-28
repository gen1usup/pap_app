package com.dadnavigator.app.presentation.screen.contraction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.usecase.contraction.CalculateContractionStatsUseCase
import com.dadnavigator.app.domain.usecase.contraction.FinishContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.contraction.StartContractionSessionUseCase
import com.dadnavigator.app.domain.usecase.contraction.ToggleContractionUseCase
import com.dadnavigator.app.domain.usecase.labor.MarkBirthUseCase
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
    private val toggleContractionUseCase: ToggleContractionUseCase,
    private val calculateContractionStatsUseCase: CalculateContractionStatsUseCase,
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
                ticker,
                infoState,
                errorState
            ) { activeState, now, infoRes, errorRes ->
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
                    infoRes = infoRes,
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
                infoState.value = R.string.saved
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

    fun completedContractions(): List<Contraction> {
        return uiState.value.contractions.filter { it.endedAt != null }
    }
}
