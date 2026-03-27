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
 * ViewModel for manually managed emergency and hospital contacts.
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

    private var persistedContacts: List<EmergencyContact> = emptyList()
    private var nextTempId = -1L

    init {
        viewModelScope.launch(ioDispatcher) {
            seedEmergencyContactsUseCase()
        }
        viewModelScope.launch {
            observeEmergencyContactsUseCase().collect { contacts ->
                persistedContacts = contacts.sortedBy { it.sortOrder }
                if (_uiState.value.dirtyContactIds.isEmpty()) {
                    _uiState.update { current ->
                        current.copy(contacts = persistedContacts)
                    }
                }
            }
        }
    }

    fun addContact() {
        val newContact = EmergencyContact(
            id = nextTempId--,
            type = EmergencyContactType.CUSTOM,
            title = "",
            phone = "",
            address = "",
            sortOrder = uiState.value.contacts.size,
            isDefault = false
        )
        _uiState.update { current ->
            current.copy(
                contacts = (current.contacts + newContact).reindexed(),
                dirtyContactIds = current.dirtyContactIds + newContact.id,
                infoRes = null,
                errorRes = null
            )
        }
    }

    fun updateTitle(contactId: Long, value: String) {
        updateContact(contactId) { contact -> contact.copy(title = value) }
    }

    fun updatePhone(contactId: Long, value: String) {
        if (isEmergency(contactId)) return
        updateContact(contactId) { contact -> contact.copy(phone = value) }
    }

    fun updateAddress(contactId: Long, value: String) {
        updateContact(contactId) { contact -> contact.copy(address = value) }
    }

    fun saveContact(contactId: Long) {
        val contacts = uiState.value.contacts
        val contact = contacts.firstOrNull { it.id == contactId } ?: return
        if (!canSave(contact)) {
            _uiState.update { it.copy(errorRes = R.string.input_required, infoRes = null) }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val contactsToSave = contacts.reindexed()
                saveEmergencyContactsUseCase(contactsToSave)
                _uiState.update { current ->
                    current.copy(
                        contacts = contactsToSave,
                        dirtyContactIds = emptySet(),
                        infoRes = R.string.saved,
                        errorRes = null
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic, infoRes = null) }
            }
        }
    }

    fun deleteContact(contactId: Long) {
        val contact = uiState.value.contacts.firstOrNull { it.id == contactId } ?: return
        if (contact.isDefault) return

        val remaining = uiState.value.contacts
            .filterNot { it.id == contactId }
            .reindexed()

        viewModelScope.launch(ioDispatcher) {
            runCatching {
                saveEmergencyContactsUseCase(remaining)
                _uiState.update {
                    it.copy(
                        contacts = remaining,
                        dirtyContactIds = it.dirtyContactIds - contactId,
                        infoRes = R.string.saved,
                        errorRes = null
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(errorRes = R.string.error_generic, infoRes = null) }
            }
        }
    }

    fun isDirty(contactId: Long): Boolean = uiState.value.dirtyContactIds.contains(contactId)

    fun canSave(contact: EmergencyContact): Boolean {
        return when {
            contact.type == EmergencyContactType.EMERGENCY -> false
            contact.type == EmergencyContactType.HOSPITAL -> true
            else -> contact.title.trim().isNotBlank()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(infoRes = null, errorRes = null) }
    }

    private fun updateContact(
        contactId: Long,
        transform: (EmergencyContact) -> EmergencyContact
    ) {
        _uiState.update { current ->
            val updatedContacts = current.contacts.map { contact ->
                if (contact.id == contactId) {
                    transform(contact)
                } else {
                    contact
                }
            }
            current.copy(
                contacts = updatedContacts,
                dirtyContactIds = current.dirtyContactIds.updateDirtyIds(
                    contactId = contactId,
                    updatedContacts = updatedContacts,
                    persistedContacts = persistedContacts
                ),
                infoRes = null,
                errorRes = null
            )
        }
    }

    private fun Set<Long>.updateDirtyIds(
        contactId: Long,
        updatedContacts: List<EmergencyContact>,
        persistedContacts: List<EmergencyContact>
    ): Set<Long> {
        val updated = updatedContacts.firstOrNull { it.id == contactId } ?: return this - contactId
        val persisted = persistedContacts.firstOrNull { it.id == contactId }
        val isDirty = persisted == null || updated.normalizedForComparison() != persisted.normalizedForComparison()
        return if (isDirty) this + contactId else this - contactId
    }

    private fun List<EmergencyContact>.reindexed(): List<EmergencyContact> {
        return sortedWith(compareBy<EmergencyContact> { requiredPosition(it) }.thenBy { it.sortOrder }.thenBy { it.id })
            .mapIndexed { index, contact -> contact.copy(sortOrder = index) }
    }

    private fun requiredPosition(contact: EmergencyContact): Int = when (contact.type) {
        EmergencyContactType.EMERGENCY -> 0
        EmergencyContactType.HOSPITAL -> 1
        else -> 2
    }

    private fun EmergencyContact.normalizedForComparison(): EmergencyContact {
        return copy(
            title = title.trim(),
            phone = phone.trim(),
            address = address.trim(),
            sortOrder = requiredPosition(this)
        )
    }

    private fun isEmergency(contactId: Long): Boolean {
        return uiState.value.contacts.any { it.id == contactId && it.type == EmergencyContactType.EMERGENCY }
    }
}
