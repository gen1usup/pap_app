package com.dadnavigator.app.core.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val CloudLight = Color(0xFFF3F6F6)
val CloudDark = Color(0xFF12181C)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1A2227)
val MistLight = Color(0xFFE5EDEC)
val MistDark = Color(0xFF233038)

val CalmPrimaryLight = Color(0xFF2F6F6A)
val CalmOnPrimaryLight = Color(0xFFFFFFFF)
val CalmPrimaryContainerLight = Color(0xFFD0ECE8)
val CalmOnPrimaryContainerLight = Color(0xFF123632)

val CalmSecondaryLight = Color(0xFF526B78)
val CalmOnSecondaryLight = Color(0xFFFFFFFF)
val CalmSecondaryContainerLight = Color(0xFFD7E6EE)
val CalmOnSecondaryContainerLight = Color(0xFF132833)

val CalmTertiaryLight = Color(0xFF86663A)
val CalmOnTertiaryLight = Color(0xFFFFFFFF)
val CalmTertiaryContainerLight = Color(0xFFF6E1BB)
val CalmOnTertiaryContainerLight = Color(0xFF2C1C00)

val CalmPrimaryDark = Color(0xFF82C8BF)
val CalmOnPrimaryDark = Color(0xFF0F3431)
val CalmPrimaryContainerDark = Color(0xFF234A47)
val CalmOnPrimaryContainerDark = Color(0xFFD5F3EE)

val CalmSecondaryDark = Color(0xFFB4CBD8)
val CalmOnSecondaryDark = Color(0xFF1F3440)
val CalmSecondaryContainerDark = Color(0xFF334854)
val CalmOnSecondaryContainerDark = Color(0xFFD7E6EE)

val CalmTertiaryDark = Color(0xFFE1C28E)
val CalmOnTertiaryDark = Color(0xFF44300F)
val CalmTertiaryContainerDark = Color(0xFF5B4522)
val CalmOnTertiaryContainerDark = Color(0xFFF8E2BA)

val CriticalLight = Color(0xFFB65056)
val CriticalContainerLight = Color(0xFFFFD9DA)
val CriticalOnLight = Color(0xFFFFFFFF)
val CriticalOnContainerLight = Color(0xFF3D060D)

val CriticalDark = Color(0xFFF2B1B3)
val CriticalContainerDark = Color(0xFF7A3237)
val CriticalOnDark = Color(0xFF4B1218)
val CriticalOnContainerDark = Color(0xFFFFD9DA)

val WarningLight = Color(0xFF937040)
val WarningContainerLight = Color(0xFFF5E2BF)
val WarningOnLight = Color(0xFFFFFFFF)
val WarningOnContainerLight = Color(0xFF2E1F04)

val WarningDark = Color(0xFFE0C187)
val WarningContainerDark = Color(0xFF604A23)
val WarningOnDark = Color(0xFF3B2A08)
val WarningOnContainerDark = Color(0xFFF5E2BF)

val SuccessLight = Color(0xFF4E7F67)
val SuccessContainerLight = Color(0xFFD2ECDD)
val SuccessOnLight = Color(0xFFFFFFFF)
val SuccessOnContainerLight = Color(0xFF11261B)

val SuccessDark = Color(0xFFA8D0B8)
val SuccessContainerDark = Color(0xFF335740)
val SuccessOnDark = Color(0xFF183120)
val SuccessOnContainerDark = Color(0xFFD2ECDD)

@Immutable
data class DadStatusColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val critical: Color,
    val onCritical: Color,
    val criticalContainer: Color,
    val onCriticalContainer: Color
)
