package com.dadnavigator.app.domain.usecase.emergency

import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.repository.EmergencyContactRepository
import javax.inject.Inject

/**
 * Persists edited emergency contacts.
 */
class SaveEmergencyContactsUseCase @Inject constructor(
    private val emergencyContactRepository: EmergencyContactRepository
) {
    suspend operator fun invoke(contacts: List<EmergencyContact>) {
        emergencyContactRepository.saveContacts(contacts)
    }
}
