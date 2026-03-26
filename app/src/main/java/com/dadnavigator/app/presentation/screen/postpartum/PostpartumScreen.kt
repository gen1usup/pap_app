package com.dadnavigator.app.presentation.screen.postpartum

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
 * Postpartum care and observation guidance.
 */
@Composable
fun PostpartumScreen(onBack: () -> Unit) {
    val father = stringArrayResource(id = R.array.postpartum_father_actions).toList()
    val mother = stringArrayResource(id = R.array.postpartum_mother_observe).toList()
    val baby = stringArrayResource(id = R.array.postpartum_baby_observe).toList()
    val doctor = stringArrayResource(id = R.array.postpartum_doctor_signs).toList()

    ScreenScaffold(
        title = stringResource(id = R.string.postpartum_title),
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
                    title = stringResource(id = R.string.postpartum_for_father),
                    lines = father
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.postpartum_for_mother),
                    lines = mother
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.postpartum_for_baby),
                    lines = baby
                )
            }
            item {
                InfoSectionCard(
                    title = stringResource(id = R.string.postpartum_medical),
                    lines = doctor
                )
            }
        }
    }
}
