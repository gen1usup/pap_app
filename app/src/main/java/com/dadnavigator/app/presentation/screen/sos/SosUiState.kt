package com.dadnavigator.app.presentation.screen.sos

import com.dadnavigator.app.domain.model.EmergencyContact

/**
 * Read-only emergency surface state for SOS quick actions.
 */
data class SosUiState(
    val contacts: List<EmergencyContact> = emptyList()
)
