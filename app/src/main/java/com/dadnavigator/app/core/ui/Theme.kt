package com.dadnavigator.app.core.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = CalmPrimaryLight,
    onPrimary = CalmOnPrimaryLight,
    primaryContainer = CalmPrimaryContainerLight,
    onPrimaryContainer = CalmOnPrimaryContainerLight,
    secondary = CalmSecondaryLight,
    onSecondary = CalmOnSecondaryLight,
    secondaryContainer = CalmSecondaryContainerLight,
    onSecondaryContainer = CalmOnSecondaryContainerLight,
    tertiary = CalmTertiaryLight,
    onTertiary = CalmOnTertiaryLight,
    tertiaryContainer = CalmTertiaryContainerLight,
    onTertiaryContainer = CalmOnTertiaryContainerLight,
    background = CloudLight,
    onBackground = ColorTokens.TextStrongLight,
    surface = SurfaceLight,
    onSurface = ColorTokens.TextStrongLight,
    surfaceVariant = MistLight,
    onSurfaceVariant = ColorTokens.TextMutedLight,
    surfaceContainer = ColorTokens.SurfaceContainerLight,
    surfaceContainerLow = ColorTokens.SurfaceContainerLowLight,
    surfaceContainerHigh = ColorTokens.SurfaceContainerHighLight,
    error = CriticalLight,
    onError = CriticalOnLight,
    errorContainer = CriticalContainerLight,
    onErrorContainer = CriticalOnContainerLight,
    outline = ColorTokens.OutlineLight,
    outlineVariant = ColorTokens.OutlineVariantLight
)

private val DarkColors = darkColorScheme(
    primary = CalmPrimaryDark,
    onPrimary = CalmOnPrimaryDark,
    primaryContainer = CalmPrimaryContainerDark,
    onPrimaryContainer = CalmOnPrimaryContainerDark,
    secondary = CalmSecondaryDark,
    onSecondary = CalmOnSecondaryDark,
    secondaryContainer = CalmSecondaryContainerDark,
    onSecondaryContainer = CalmOnSecondaryContainerDark,
    tertiary = CalmTertiaryDark,
    onTertiary = CalmOnTertiaryDark,
    tertiaryContainer = CalmTertiaryContainerDark,
    onTertiaryContainer = CalmOnTertiaryContainerDark,
    background = CloudDark,
    onBackground = ColorTokens.TextStrongDark,
    surface = SurfaceDark,
    onSurface = ColorTokens.TextStrongDark,
    surfaceVariant = MistDark,
    onSurfaceVariant = ColorTokens.TextMutedDark,
    surfaceContainer = ColorTokens.SurfaceContainerDark,
    surfaceContainerLow = ColorTokens.SurfaceContainerLowDark,
    surfaceContainerHigh = ColorTokens.SurfaceContainerHighDark,
    error = CriticalDark,
    onError = CriticalOnDark,
    errorContainer = CriticalContainerDark,
    onErrorContainer = CriticalOnContainerDark,
    outline = ColorTokens.OutlineDark,
    outlineVariant = ColorTokens.OutlineVariantDark
)

private object ColorTokens {
    val TextStrongLight = androidx.compose.ui.graphics.Color(0xFF182226)
    val TextMutedLight = androidx.compose.ui.graphics.Color(0xFF5C6B72)
    val SurfaceContainerLowLight = androidx.compose.ui.graphics.Color(0xFFEAF0F1)
    val SurfaceContainerLight = androidx.compose.ui.graphics.Color(0xFFE2EAEB)
    val SurfaceContainerHighLight = androidx.compose.ui.graphics.Color(0xFFD7E1E3)
    val OutlineLight = androidx.compose.ui.graphics.Color(0xFF87979E)
    val OutlineVariantLight = androidx.compose.ui.graphics.Color(0xFFC8D4D8)

    val TextStrongDark = androidx.compose.ui.graphics.Color(0xFFE6EEF1)
    val TextMutedDark = androidx.compose.ui.graphics.Color(0xFFB7C5CB)
    val SurfaceContainerLowDark = androidx.compose.ui.graphics.Color(0xFF1E272C)
    val SurfaceContainerDark = androidx.compose.ui.graphics.Color(0xFF253037)
    val SurfaceContainerHighDark = androidx.compose.ui.graphics.Color(0xFF2E3A42)
    val OutlineDark = androidx.compose.ui.graphics.Color(0xFF899AA1)
    val OutlineVariantDark = androidx.compose.ui.graphics.Color(0xFF39454C)
}

private val LightStatusColors = DadStatusColors(
    success = SuccessLight,
    onSuccess = SuccessOnLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = SuccessOnContainerLight,
    warning = WarningLight,
    onWarning = WarningOnLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = WarningOnContainerLight,
    critical = CriticalLight,
    onCritical = CriticalOnLight,
    criticalContainer = CriticalContainerLight,
    onCriticalContainer = CriticalOnContainerLight
)

private val DarkStatusColors = DadStatusColors(
    success = SuccessDark,
    onSuccess = SuccessOnDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = SuccessOnContainerDark,
    warning = WarningDark,
    onWarning = WarningOnDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = WarningOnContainerDark,
    critical = CriticalDark,
    onCritical = CriticalOnDark,
    criticalContainer = CriticalContainerDark,
    onCriticalContainer = CriticalOnContainerDark
)

private val LocalDadStatusColors = staticCompositionLocalOf { LightStatusColors }
private val LocalDadShapes = staticCompositionLocalOf { DadShapeTokens() }

@Immutable
data class DadThemeTokens(
    val spacing: DadSpacing,
    val shapes: DadShapeTokens,
    val status: DadStatusColors
)

object DadTheme {
    val spacing: DadSpacing
        @Composable get() = LocalDadSpacing.current

    val shapes: DadShapeTokens
        @Composable get() = LocalDadShapes.current

    val status: DadStatusColors
        @Composable get() = LocalDadStatusColors.current
}

@Composable
fun DadNavigatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(
        LocalDadSpacing provides DadSpacing(),
        LocalDadStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors,
        LocalDadShapes provides DadShapeTokens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DadTypography,
            shapes = DadShapes,
            content = content
        )
    }
}
