package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dadnavigator.app.domain.model.AppStage
import java.time.Instant

/**
 * Room entity for user profile.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val createdAt: Instant
)

/**
 * Room entity for settings snapshot for future sync support.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val userId: String,
    val themeMode: String,
    val fatherName: String,
    val dueDateEpochDay: Long?,
    val maternityHospitalAddress: String,
    val notificationsEnabled: Boolean,
    val appStage: String,
    val updatedAt: Instant
)

/**
 * Room entity for labor summary fields.
 */
@Entity(tableName = "labor_summary")
data class LaborSummaryEntity(
    @PrimaryKey val userId: String,
    val laborStartTime: Instant?,
    val birthTime: Instant?,
    val babyName: String?,
    val birthWeightGrams: Int?,
    val birthHeightCm: Int?
)
