package com.dadnavigator.app.presentation.screen.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.presentation.component.InfoSectionCard
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    val preparing = stringArrayResource(id = R.array.help_preparing).toList()
    val labor = stringArrayResource(id = R.array.help_labor).toList()
    val afterBirth = stringArrayResource(id = R.array.help_after_birth).toList()
    val snackbarHostState = remember { SnackbarHostState() }

    ScreenScaffold(
        title = stringResource(id = R.string.help_title),
        subtitle = stringResource(id = R.string.help_subtitle),
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = DadTheme.spacing.md, vertical = DadTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DadTheme.spacing.md)
            ) {
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.app_stage_preparing),
                        lines = preparing,
                        icon = Icons.Outlined.Schedule
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.app_stage_labor),
                        lines = labor,
                        icon = Icons.Outlined.MonitorHeart
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.app_stage_baby_born),
                        lines = afterBirth,
                        icon = Icons.Outlined.ChildCare
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.help_how_app_works),
                        lines = listOf(
                            stringResource(id = R.string.help_how_app_works_1),
                            stringResource(id = R.string.help_how_app_works_2),
                            stringResource(id = R.string.help_how_app_works_3)
                        ),
                        icon = Icons.Outlined.Info
                    )
                }
            }
        }
    }
}

