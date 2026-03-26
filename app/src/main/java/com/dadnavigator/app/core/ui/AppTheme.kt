package com.dadnavigator.app.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF2F5D50),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB7E7D7),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF5C6B73),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDDE3E8),
    onSecondaryContainer = Color(0xFF192126),
    tertiary = Color(0xFF8C6D46),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF6F7F5),
    onBackground = Color(0xFF1A1C1B),
    surface = Color(0xFFF6F7F5),
    onSurface = Color(0xFF1A1C1B),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF9AD1C0),
    onPrimary = Color(0xFF00382D),
    primaryContainer = Color(0xFF155144),
    onPrimaryContainer = Color(0xFFB7E7D7),
    secondary = Color(0xFFBEC7CD),
    onSecondary = Color(0xFF283238),
    secondaryContainer = Color(0xFF3E4A51),
    onSecondaryContainer = Color(0xFFDDE3E8),
    tertiary = Color(0xFFFFDDB8),
    onTertiary = Color(0xFF4F2A08),
    background = Color(0xFF111414),
    onBackground = Color(0xFFE2E3E1),
    surface = Color(0xFF111414),
    onSurface = Color(0xFFE2E3E1),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

/**
 * App-level Compose theme with calm color palette for stressful scenarios.
 */
@Composable
fun DadNavigatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = DadTypography,
        content = content
    )
}
