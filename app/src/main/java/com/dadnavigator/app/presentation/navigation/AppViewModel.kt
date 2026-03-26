package com.dadnavigator.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Provides global app configuration for Compose host.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase
) : ViewModel() {

    val uiState: StateFlow<AppUiState> = observeSettingsUseCase().map { settings ->
        AppUiState(
            userId = settings.userId.ifBlank { DEFAULT_USER_ID },
            themeMode = settings.themeMode,
            fatherName = settings.fatherName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiState()
    )
}

/**
 * UI state for root app shell.
 */
data class AppUiState(
    val userId: String = DEFAULT_USER_ID,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fatherName: String = ""
)
