package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.EmergencyContactEntity
import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType

/**
 * Maps emergency contact storage model.
 */
fun EmergencyContactEntity.toDomain(): EmergencyContact = EmergencyContact(
    type = runCatching { EmergencyContactType.valueOf(type) }
        .getOrDefault(EmergencyContactType.TRUSTED_PERSON),
    title = title,
    phone = phone,
    sortOrder = sortOrder
)

fun EmergencyContact.toEntity(): EmergencyContactEntity = EmergencyContactEntity(
    type = type.name,
    title = title,
    phone = phone,
    sortOrder = sortOrder
)
