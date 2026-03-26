package com.dadnavigator.app.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.domain.usecase.settings.ResetAllDataUseCase
import com.dadnavigator.app.domain.usecase.settings.SaveSettingsUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for app settings and reset operations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val resetAllDataUseCase: ResetAllDataUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                _uiState.update {
                    it.copy(
                        userId = settings.userId,
                        fatherName = if (it.fatherName.isBlank()) settings.fatherName else it.fatherName,
                        dueDateInput = if (it.dueDateInput.isBlank()) {
                            settings.dueDate?.toString().orEmpty()
                        } else {
                            it.dueDateInput
                        },
                        notificationsEnabled = settings.notificationsEnabled,
                        themeMode = settings.themeMode
                    )
                }
            }
        }
    }

    fun updateFatherName(value: String) {
        _uiState.update { it.copy(fatherName = value) }
    }

    fun updateDueDate(value: String) {
        _uiState.update { it.copy(dueDateInput = value) }
    }

    fun updateNotifications(value: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = value) }
    }

    fun updateThemeMode(value: ThemeMode) {
        _uiState.update { it.copy(themeMode = value) }
    }

    fun save() {
        val current = _uiState.value
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val dueDate = if (current.dueDateInput.isBlank()) {
                    null
                } else {
                    parseDate(current.dueDateInput)
                }
                saveSettingsUseCase(
                    Settings(
                        userId = current.userId,
                        themeMode = current.themeMode,
                        fatherName = current.fatherName.trim(),
                        dueDate = dueDate,
                        notificationsEnabled = current.notificationsEnabled
                    )
                )
                _uiState.update { it.copy(infoRes = R.string.saved, errorRes = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorRes = if (error is IllegalArgumentException) R.string.invalid_date else R.string.error_generic
                    )
                }
            }
        }
    }

    fun askResetConfirmation() {
        _uiState.update { it.copy(showResetDialog = true) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(showResetDialog = false) }
    }

    fun resetAllData() {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                resetAllDataUseCase()
                _uiState.update {
                    SettingsUiState(
                        infoRes = R.string.settings_reset_done
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoRes = null, errorRes = null) }
    }

    private fun parseDate(value: String): LocalDate {
        return try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date")
        }
    }
}
