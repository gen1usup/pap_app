package com.dadnavigator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dadnavigator.app.core.ui.DadNavigatorTheme
import com.dadnavigator.app.domain.model.ThemeMode
import com.dadnavigator.app.presentation.navigation.AppNavHost
import com.dadnavigator.app.presentation.navigation.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point hosting Compose navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val appState by appViewModel.uiState.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)
            val darkTheme = when (appState.themeMode) {
                ThemeMode.SYSTEM -> null
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            DadNavigatorTheme(
                darkTheme = darkTheme ?: isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                AppNavHost(
                    userId = appState.userId,
                    widthSizeClass = windowSizeClass.widthSizeClass,
                    appStage = appState.appStage,
                    onUpdateAppStage = appViewModel::updateAppStage
                )
            }
        }
    }
}
