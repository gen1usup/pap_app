package com.dadnavigator.app.domain.usecase.emergency

import com.dadnavigator.app.domain.repository.EmergencyContactRepository
import javax.inject.Inject

/**
 * Ensures default quick-dial contacts exist.
 */
class SeedEmergencyContactsUseCase @Inject constructor(
    private val emergencyContactRepository: EmergencyContactRepository
) {
    suspend operator fun invoke() {
        emergencyContactRepository.seedContactsIfNeeded()
    }
}
