package com.dadnavigator.app.domain.model

import java.time.LocalDate

/**
 * Persistent app settings stored via DataStore.
 */
data class Settings(
    val userId: String,
    val themeMode: ThemeMode,
    val fatherName: String,
    val dueDate: LocalDate?,
    val notificationsEnabled: Boolean
)
