package com.dadnavigator.app.presentation.screen.emergencycontacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType
import com.dadnavigator.app.domain.usecase.emergency.ObserveEmergencyContactsUseCase
import com.dadnavigator.app.domain.usecase.emergency.SaveEmergencyContactsUseCase
import com.dadnavigator.app.domain.usecase.emergency.SeedEmergencyContactsUseCase
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
 * ViewModel for editable emergency quick-dial contacts.
 */
@HiltViewModel
class EmergencyContactsViewModel @Inject constructor(
    observeEmergencyContactsUseCase: ObserveEmergencyContactsUseCase,
    private val saveEmergencyContactsUseCase: SaveEmergencyContactsUseCase,
    private val seedEmergencyContactsUseCase: SeedEmergencyContactsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyContactsUiState())
    val uiState: StateFlow<EmergencyContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            seedEmergencyContactsUseCase()
        }
        viewModelScope.launch {
            observeEmergencyContactsUseCase().collect { contacts ->
                _uiState.update { current ->
                    current.copy(
                        contacts = if (current.contacts.isEmpty()) contacts else current.contacts,
                        expandedTypes = current.expandedTypes + contacts
                            .filter { it.type == EmergencyContactType.AMBULANCE || it.phone.isNotBlank() }
                            .map { it.type }
                    )
                }
            }
        }
    }

    fun updateTitle(type: EmergencyContactType, value: String) {
        updateContact(type) { it.copy(title = value) }
    }

    fun updatePhone(type: EmergencyContactType, value: String) {
        updateContact(type) { it.copy(phone = value) }
    }

    fun save() {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                saveEmergencyContactsUseCase(uiState.value.contacts)
                _uiState.update { it.copy(infoRes = R.string.saved, errorRes = null) }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoRes = null, errorRes = null) }
    }

    fun showContact(type: EmergencyContactType) {
        _uiState.update { current ->
            current.copy(expandedTypes = current.expandedTypes + type)
        }
    }

    private fun updateContact(
        type: EmergencyContactType,
        transform: (EmergencyContact) -> EmergencyContact
    ) {
        _uiState.update { current ->
            current.copy(
                contacts = current.contacts.map { contact ->
                    if (contact.type == type) transform(contact) else contact
                }
            )
        }
    }
}
