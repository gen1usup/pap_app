package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for quick-access emergency contacts.
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey val type: String,
    val title: String,
    val phone: String,
    val sortOrder: Int
)
