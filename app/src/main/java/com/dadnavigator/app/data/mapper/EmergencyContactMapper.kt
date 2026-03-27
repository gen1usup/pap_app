package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.EmergencyContactEntity
import com.dadnavigator.app.domain.model.EmergencyContact
import com.dadnavigator.app.domain.model.EmergencyContactType

/**
 * Maps emergency contact storage model.
 */
fun EmergencyContactEntity.toDomain(): EmergencyContact = EmergencyContact(
    id = id,
    type = EmergencyContactType.fromStorage(type),
    title = title,
    phone = phone,
    address = address,
    sortOrder = sortOrder,
    isDefault = isDefault
)

fun EmergencyContact.toEntity(): EmergencyContactEntity = EmergencyContactEntity(
    id = id,
    type = type.name,
    title = title,
    phone = phone,
    address = address,
    sortOrder = sortOrder,
    isDefault = isDefault
)
