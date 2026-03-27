package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.EmergencyContactDao
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.data.mapper.toEntity
import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType
import com.dadnavigator.app.domain.repository.EmergencyContactRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed repository for quick dial emergency contacts.
 */
class EmergencyContactRepositoryImpl @Inject constructor(
    private val emergencyContactDao: EmergencyContactDao
) : EmergencyContactRepository {

    override fun observeContacts(): Flow<List<EmergencyContact>> {
        return emergencyContactDao.observeContacts().map { contacts -> contacts.map { it.toDomain() } }
    }

    override suspend fun seedContactsIfNeeded() {
        if (emergencyContactDao.countContacts() > 0) return

        emergencyContactDao.replaceContacts(
            listOf(
                EmergencyContact(
                    id = 0,
                    type = EmergencyContactType.EMERGENCY,
                    title = "Скорая помощь",
                    phone = "112",
                    address = "",
                    sortOrder = 0,
                    isDefault = true
                )
            ).map { it.toEntity() }
        )
    }

    override suspend fun saveContacts(contacts: List<EmergencyContact>) {
        emergencyContactDao.replaceContacts(
            contacts.sortedBy { it.sortOrder }.mapIndexed { index, contact ->
                contact.copy(
                    title = contact.title.trim(),
                    phone = contact.phone.trim(),
                    address = contact.address.trim(),
                    sortOrder = index
                ).toEntity()
            }
        )
    }
}
