package com.dadnavigator.app.presentation.screen.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.usecase.settings.ObserveSettingsUseCase
import com.dadnavigator.app.domain.usecase.settings.SaveSettingsUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds help content state and editable maternity hospital address.
 */
@HiltViewModel
class HelpViewModel @Inject constructor(
    observeSettingsUseCase: ObserveSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private var currentSettings: Settings? = null
    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSettingsUseCase().collect { settings ->
                currentSettings = settings
                _uiState.update { state ->
                    state.copy(
                        maternityHospitalAddress = if (state.maternityHospitalAddress.isBlank()) {
                            settings.maternityHospitalAddress
                        } else {
                            state.maternityHospitalAddress
                        }
                    )
                }
            }
        }
    }

    fun updateMaternityHospitalAddress(value: String) {
        _uiState.update { it.copy(maternityHospitalAddress = value) }
    }

    fun saveMaternityHospitalAddress() {
        val settings = currentSettings ?: return
        val address = uiState.value.maternityHospitalAddress.trim()
        if (address.isBlank()) {
            _uiState.update { it.copy(errorRes = R.string.input_required, infoRes = null) }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                saveSettingsUseCase(settings.copy(maternityHospitalAddress = address))
                _uiState.update { it.copy(infoRes = R.string.saved, errorRes = null) }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic, infoRes = null) }
            }
        }
    }

    fun clearMaternityHospitalAddress() {
        val settings = currentSettings ?: return

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                saveSettingsUseCase(settings.copy(maternityHospitalAddress = ""))
                _uiState.update {
                    it.copy(
                        maternityHospitalAddress = "",
                        infoRes = R.string.saved,
                        errorRes = null
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic, infoRes = null) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoRes = null, errorRes = null) }
    }
}
