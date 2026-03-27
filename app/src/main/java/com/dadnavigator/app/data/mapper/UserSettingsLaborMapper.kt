package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.LaborSummaryEntity
import com.dadnavigator.app.data.local.entity.SettingsEntity
import com.dadnavigator.app.data.local.entity.UserEntity
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.AppUser
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.ThemeMode
import java.time.Instant

/**
 * Maps user, settings and labor metadata entities.
 */
fun UserEntity.toDomain(): AppUser = AppUser(
    id = id,
    displayName = displayName,
    createdAt = createdAt
)

fun AppUser.toEntity(): UserEntity = UserEntity(
    id = id,
    displayName = displayName,
    createdAt = createdAt
)

fun SettingsEntity.toDomain(): Settings = Settings(
    userId = userId,
    themeMode = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.SYSTEM),
    fatherName = fatherName,
    dueDate = dueDateEpochDay?.let(java.time.LocalDate::ofEpochDay),
    maternityHospitalAddress = maternityHospitalAddress,
    notificationsEnabled = notificationsEnabled,
    appStage = AppStage.fromStorage(appStage)
)

fun Settings.toEntity(updatedAt: Instant = Instant.now()): SettingsEntity = SettingsEntity(
    userId = userId,
    themeMode = themeMode.name,
    fatherName = fatherName,
    dueDateEpochDay = dueDate?.toEpochDay(),
    maternityHospitalAddress = maternityHospitalAddress,
    notificationsEnabled = notificationsEnabled,
    appStage = appStage.name,
    updatedAt = updatedAt
)

fun LaborSummaryEntity.toDomain(): LaborSummary = LaborSummary(
    laborStartTime = laborStartTime,
    birthTime = birthTime,
    babyName = babyName,
    birthWeightGrams = birthWeightGrams,
    birthHeightCm = birthHeightCm
)

fun LaborSummary.toEntity(userId: String): LaborSummaryEntity = LaborSummaryEntity(
    userId = userId,
    laborStartTime = laborStartTime,
    birthTime = birthTime,
    babyName = babyName,
    birthWeightGrams = birthWeightGrams,
    birthHeightCm = birthHeightCm
)
