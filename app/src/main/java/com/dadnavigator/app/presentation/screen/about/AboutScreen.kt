package com.dadnavigator.app.presentation.screen.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@Composable
fun AboutScreen(onBack: () -> Unit) {
    ScreenScaffold(
        title = stringResource(id = R.string.about_title),
        subtitle = stringResource(id = R.string.about_subtitle),
        onBack = onBack
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
                    InfoCard(
                        title = stringResource(id = R.string.about_offline_title),
                        description = stringResource(id = R.string.about_offline_description),
                        icon = Icons.Outlined.OfflineBolt
                    )
                }
                item {
                    InfoCard(
                        title = stringResource(id = R.string.about_privacy_title),
                        description = stringResource(id = R.string.about_privacy_description),
                        icon = Icons.Outlined.Lock
                    )
                }
                item {
                    InfoCard(
                        title = stringResource(id = R.string.about_disclaimer_title),
                        description = stringResource(id = R.string.about_disclaimer_description),
                        icon = Icons.Outlined.Info
                    )
                }
            }
        }
    }
}
