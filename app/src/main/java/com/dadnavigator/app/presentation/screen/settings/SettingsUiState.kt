package com.dadnavigator.app.presentation.screen.settings

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.ThemeMode

/**
 * UI state for settings screen.
 */
data class SettingsUiState(
    val userId: String = "",
    val fatherName: String = "",
    val dueDateInput: String = "",
    val maternityHospitalAddress: String = "",
    val notificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appStage: AppStage = AppStage.PREPARING,
    val dueDateErrorRes: Int? = null,
    val showResetDialog: Boolean = false,
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
