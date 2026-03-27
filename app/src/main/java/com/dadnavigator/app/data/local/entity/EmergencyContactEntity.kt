package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for quick-access emergency contacts.
 */
@Entity(
    tableName = "emergency_contacts",
    indices = [Index("sortOrder")]
)
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val title: String,
    val phone: String,
    val address: String,
    val sortOrder: Int,
    val isDefault: Boolean
)
