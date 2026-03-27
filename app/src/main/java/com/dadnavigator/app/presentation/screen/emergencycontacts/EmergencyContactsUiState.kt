package com.dadnavigator.app.presentation.screen.emergencycontacts

import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType

/**
 * UI state for emergency contacts editing.
 */
data class EmergencyContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val expandedTypes: Set<EmergencyContactType> = setOf(EmergencyContactType.AMBULANCE),
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
