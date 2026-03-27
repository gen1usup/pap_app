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

    private var hasLocalChanges = false
    private var nextTempId = -1L

    init {
        viewModelScope.launch(ioDispatcher) {
            seedEmergencyContactsUseCase()
        }
        viewModelScope.launch {
            observeEmergencyContactsUseCase().collect { contacts ->
                if (!hasLocalChanges || _uiState.value.contacts.isEmpty()) {
                    _uiState.update { current ->
                        current.copy(contacts = contacts.sortedBy { it.sortOrder })
                    }
                }
            }
        }
    }

    fun addContact(type: EmergencyContactType) {
        val currentContacts = uiState.value.contacts
        if (type in singleInstanceTypes && currentContacts.any { it.type == type }) return

        hasLocalChanges = true
        _uiState.update { current ->
            current.copy(
                contacts = current.contacts + EmergencyContact(
                    id = nextTempId--,
                    type = type,
                    title = defaultTitle(type),
                    phone = "",
                    address = "",
                    sortOrder = current.contacts.size,
                    isDefault = false
                )
            )
        }
    }

    fun updateTitle(contactId: Long, value: String) {
        updateContact(contactId) { it.copy(title = value) }
    }

    fun updatePhone(contactId: Long, value: String) {
        updateContact(contactId) { it.copy(phone = value) }
    }

    fun updateAddress(contactId: Long, value: String) {
        updateContact(contactId) { it.copy(address = value) }
    }

    fun deleteContact(contactId: Long) {
        val contact = uiState.value.contacts.firstOrNull { it.id == contactId } ?: return
        if (contact.isDefault) return

        hasLocalChanges = true
        _uiState.update { current ->
            current.copy(
                contacts = current.contacts
                    .filterNot { it.id == contactId }
                    .mapIndexed { index, item -> item.copy(sortOrder = index) }
            )
        }
    }

    fun save() {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                saveEmergencyContactsUseCase(
                    uiState.value.contacts
                        .sortedBy { it.sortOrder }
                        .mapIndexed { index, contact -> contact.copy(sortOrder = index) }
                )
                hasLocalChanges = false
                _uiState.update { it.copy(infoRes = R.string.saved, errorRes = null) }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic, infoRes = null) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoRes = null, errorRes = null) }
    }

    private fun updateContact(
        contactId: Long,
        transform: (EmergencyContact) -> EmergencyContact
    ) {
        hasLocalChanges = true
        _uiState.update { current ->
            current.copy(
                contacts = current.contacts.map { contact ->
                    if (contact.id == contactId) transform(contact) else contact
                }
            )
        }
    }

    private fun defaultTitle(type: EmergencyContactType): String = when (type) {
        EmergencyContactType.EMERGENCY -> "Скорая помощь"
        EmergencyContactType.WIFE -> "Жена"
        EmergencyContactType.DOCTOR -> "Врач"
        EmergencyContactType.HOSPITAL -> "Роддом"
        EmergencyContactType.TAXI -> "Такси"
        EmergencyContactType.RELATIVE -> "Родственник"
        EmergencyContactType.CUSTOM -> "Контакт"
    }

    private companion object {
        val singleInstanceTypes = setOf(
            EmergencyContactType.EMERGENCY,
            EmergencyContactType.WIFE,
            EmergencyContactType.DOCTOR,
            EmergencyContactType.HOSPITAL,
            EmergencyContactType.TAXI
        )
    }
}
