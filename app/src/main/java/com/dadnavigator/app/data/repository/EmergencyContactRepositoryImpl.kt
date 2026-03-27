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

        emergencyContactDao.upsertContacts(
            listOf(
                EmergencyContact(EmergencyContactType.AMBULANCE, "Скорая помощь", "112", 0),
                EmergencyContact(EmergencyContactType.MATERNITY_HOSPITAL, "Роддом", "", 1),
                EmergencyContact(EmergencyContactType.DOCTOR, "Врач", "", 2),
                EmergencyContact(EmergencyContactType.MIDWIFE, "Акушерка", "", 3),
                EmergencyContact(EmergencyContactType.TRUSTED_PERSON, "Доверенное лицо", "", 4),
                EmergencyContact(EmergencyContactType.PARTNER, "Жена / близкий человек", "", 5),
                EmergencyContact(EmergencyContactType.TAXI, "Такси", "", 6)
            ).map { it.toEntity() }
        )
    }

    override suspend fun saveContacts(contacts: List<EmergencyContact>) {
        emergencyContactDao.upsertContacts(
            contacts.sortedBy { it.sortOrder }.map { contact ->
                contact.copy(
                    title = contact.title.trim(),
                    phone = contact.phone.trim()
                ).toEntity()
            }
        )
    }
}
