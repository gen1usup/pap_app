package com.dadnavigator.app.presentation.screen.emergencycontacts

import com.dadnavigator.app.domain.model.EmergencyContact

/**
 * UI state for emergency contacts editing.
 */
data class EmergencyContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
