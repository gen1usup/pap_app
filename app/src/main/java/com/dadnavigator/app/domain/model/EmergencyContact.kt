package com.dadnavigator.app.domain.model

/**
 * Contact role used by quick actions and emergency flows.
 */
enum class EmergencyContactType {
    EMERGENCY,
    WIFE,
    DOCTOR,
    HOSPITAL,
    TAXI,
    RELATIVE,
    CUSTOM;

    companion object {
        fun fromStorage(value: String): EmergencyContactType = when (value) {
            "EMERGENCY", "AMBULANCE" -> EMERGENCY
            "HOSPITAL", "MATERNITY_HOSPITAL" -> HOSPITAL
            "WIFE", "PARTNER" -> WIFE
            "DOCTOR" -> DOCTOR
            "TAXI" -> TAXI
            "RELATIVE", "MIDWIFE", "TRUSTED_PERSON" -> RELATIVE
            "CUSTOM" -> CUSTOM
            else -> CUSTOM
        }
    }
}

/**
 * Editable emergency or logistics contact stored locally for fast access.
 */
data class EmergencyContact(
    val id: Long,
    val type: EmergencyContactType,
    val title: String,
    val phone: String,
    val address: String,
    val sortOrder: Int,
    val isDefault: Boolean
)
