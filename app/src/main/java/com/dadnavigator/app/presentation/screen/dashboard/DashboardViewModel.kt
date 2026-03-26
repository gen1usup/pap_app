package com.dadnavigator.app.presentation.screen.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.usecase.contraction.ObserveContractionStateUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.ObserveActiveWaterBreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Provides dashboard context in stressful scenarios.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeContractionStateUseCase: ObserveContractionStateUseCase,
    private val observeActiveWaterBreakUseCase: ObserveActiveWaterBreakUseCase
) : ViewModel() {

    private val userIdState = MutableStateFlow(DEFAULT_USER_ID)

    val uiState = userIdState
        .flatMapLatest { userId ->
            combine(
                observeContractionStateUseCase(userId),
                observeActiveWaterBreakUseCase(userId)
            ) { contractionState, waterBreakEvent ->
                DashboardUiState(
                    hasActiveContractionSession = contractionState.session?.isActive == true,
                    hasActiveWaterBreak = waterBreakEvent?.isActive == true,
                    currentActionRes = when {
                        waterBreakEvent?.isActive == true -> R.string.now_action_water_break
                        contractionState.session?.isActive == true -> R.string.now_action_contraction
                        else -> R.string.now_action_default
                    }
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
}

/**
 * Dashboard UI state.
 */
data class DashboardUiState(
    val hasActiveContractionSession: Boolean = false,
    val hasActiveWaterBreak: Boolean = false,
    @StringRes val currentActionRes: Int = R.string.now_action_default
)


