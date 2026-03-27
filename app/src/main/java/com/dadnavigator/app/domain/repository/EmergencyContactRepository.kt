package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

/**
 * Contract for editable emergency contacts.
 */
interface EmergencyContactRepository {
    fun observeContacts(): Flow<List<EmergencyContact>>

    suspend fun seedContactsIfNeeded()

    suspend fun saveContacts(contacts: List<EmergencyContact>)
}
