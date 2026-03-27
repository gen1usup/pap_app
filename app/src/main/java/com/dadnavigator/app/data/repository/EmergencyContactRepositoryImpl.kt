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
        val currentContacts = emergencyContactDao.getContacts().map { it.toDomain() }
        val normalized = ensureRequiredContacts(currentContacts)

        if (currentContacts != normalized) {
            emergencyContactDao.replaceContacts(normalized.map { it.toEntity() })
        }
    }

    override suspend fun saveContacts(contacts: List<EmergencyContact>) {
        emergencyContactDao.replaceContacts(
            ensureRequiredContacts(contacts).mapIndexed { index, contact ->
                contact.copy(
                    id = if (contact.id < 0) 0 else contact.id,
                    title = contact.title.trim(),
                    phone = contact.phone.trim(),
                    address = contact.address.trim(),
                    sortOrder = index
                ).toEntity()
            }
        )
    }

    private fun ensureRequiredContacts(contacts: List<EmergencyContact>): List<EmergencyContact> {
        val emergency = contacts.firstOrNull { it.type == EmergencyContactType.EMERGENCY }?.copy(
            title = EMERGENCY_TITLE,
            phone = EMERGENCY_PHONE,
            address = "",
            isDefault = true
        ) ?: EmergencyContact(
            id = 0,
            type = EmergencyContactType.EMERGENCY,
            title = EMERGENCY_TITLE,
            phone = EMERGENCY_PHONE,
            address = "",
            sortOrder = 0,
            isDefault = true
        )
        val hospital = contacts.firstOrNull { it.type == EmergencyContactType.HOSPITAL }?.copy(
            title = HOSPITAL_TITLE,
            isDefault = true
        ) ?: EmergencyContact(
            id = 0,
            type = EmergencyContactType.HOSPITAL,
            title = HOSPITAL_TITLE,
            phone = "",
            address = "",
            sortOrder = 1,
            isDefault = true
        )
        val others = contacts
            .filterNot { it.type == EmergencyContactType.EMERGENCY || it.type == EmergencyContactType.HOSPITAL }
            .map { it.copy(isDefault = false) }

        return buildList {
            add(emergency)
            add(hospital)
            addAll(others)
        }.mapIndexed { index, contact ->
            contact.copy(sortOrder = index)
        }
    }

    private companion object {
        const val EMERGENCY_TITLE = "Скорая помощь"
        const val EMERGENCY_PHONE = "112"
        const val HOSPITAL_TITLE = "Роддом"
    }
}
