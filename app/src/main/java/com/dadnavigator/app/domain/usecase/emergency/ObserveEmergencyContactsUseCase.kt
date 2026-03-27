package com.dadnavigator.app.domain.usecase.emergency

import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.repository.EmergencyContactRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Observes emergency contacts configured for the device.
 */
class ObserveEmergencyContactsUseCase @Inject constructor(
    private val emergencyContactRepository: EmergencyContactRepository
) {
    operator fun invoke(): Flow<List<EmergencyContact>> = emergencyContactRepository.observeContacts()
}
