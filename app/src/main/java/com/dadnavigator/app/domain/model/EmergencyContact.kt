package com.dadnavigator.app.domain.model

/**
 * Fixed contact slots for emergency and logistics flows.
 */
enum class EmergencyContactType {
    AMBULANCE,
    MATERNITY_HOSPITAL,
    DOCTOR,
    MIDWIFE,
    TRUSTED_PERSON,
    PARTNER,
    TAXI
}

/**
 * Editable emergency contact stored locally for fast access.
 */
data class EmergencyContact(
    val type: EmergencyContactType,
    val title: String,
    val phone: String,
    val sortOrder: Int
)
