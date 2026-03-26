package com.dadnavigator.app.presentation.screen.momsupport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dadnavigator.app.R
import com.dadnavigator.app.presentation.component.InfoSectionCard
import com.dadnavigator.app.presentation.component.ScreenScaffold

/**
 * Practical support tips for helping the mother.
 */
@Composable
fun MomSupportScreen(onBack: () -> Unit) {
    val contractionsTips = stringArrayResource(id = R.array.mom_support_contractions_tips).toList()
    val breathingTips = stringArrayResource(id = R.array.mom_support_breathing_tips).toList()
    val wordsTips = stringArrayResource(id = R.array.mom_support_words_tips).toList()
    val massageTips = stringArrayResource(id = R.array.mom_support_massage_tips).toList()
    val presenceTips = stringArrayResource(id = R.array.mom_support_presence_tips).toList()

    ScreenScaffold(
        title = stringResource(id = R.string.mom_support_title),
        onBack = onBack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.mom_support_contractions),
                    lines = contractionsTips
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.mom_support_breathing),
                    lines = breathingTips
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.mom_support_words),
                    lines = wordsTips
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.mom_support_massage),
                    lines = massageTips
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.mom_support_presence),
                    lines = presenceTips
                )
            }
        }
    }
}
