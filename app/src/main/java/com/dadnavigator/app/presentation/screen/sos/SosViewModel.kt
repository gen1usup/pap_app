package com.dadnavigator.app.presentation.screen.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.domain.usecase.emergency.ObserveEmergencyContactsUseCase
import com.dadnavigator.app.domain.usecase.emergency.SeedEmergencyContactsUseCase
import com.dadnavigator.app.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes quick-dial emergency contacts for the SOS screen.
 */
@HiltViewModel
class SosViewModel @Inject constructor(
    observeEmergencyContactsUseCase: ObserveEmergencyContactsUseCase,
    private val seedEmergencyContactsUseCase: SeedEmergencyContactsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val uiState = observeEmergencyContactsUseCase()
        .map { contacts -> SosUiState(contacts = contacts) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SosUiState()
        )

    init {
        viewModelScope.launch(ioDispatcher) {
            seedEmergencyContactsUseCase()
        }
    }
}
