package com.dadnavigator.app.presentation.screen.momsupport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Wc
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.presentation.component.InfoSectionCard
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold

@Composable
fun MomSupportScreen(onBack: () -> Unit) {
    val contractionsTips = stringArrayResource(id = R.array.mom_support_contractions_tips).toList()
    val breathingTips = stringArrayResource(id = R.array.mom_support_breathing_tips).toList()
    val wordsTips = stringArrayResource(id = R.array.mom_support_words_tips).toList()
    val massageTips = stringArrayResource(id = R.array.mom_support_massage_tips).toList()
    val presenceTips = stringArrayResource(id = R.array.mom_support_presence_tips).toList()

    ScreenScaffold(
        title = stringResource(id = R.string.mom_support_title),
        subtitle = stringResource(id = R.string.mom_support_subtitle),
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
                    InfoSectionCard(
                        title = stringResource(id = R.string.mom_support_contractions),
                        lines = contractionsTips,
                        icon = Icons.Outlined.Wc
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.mom_support_breathing),
                        lines = breathingTips,
                        icon = Icons.Outlined.SelfImprovement
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.mom_support_words),
                        lines = wordsTips,
                        icon = Icons.Outlined.RecordVoiceOver
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.mom_support_massage),
                        lines = massageTips,
                        icon = Icons.Outlined.PanTool
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.mom_support_presence),
                        lines = presenceTips,
                        icon = Icons.Outlined.FavoriteBorder
                    )
                }
            }
        }
    }
}
