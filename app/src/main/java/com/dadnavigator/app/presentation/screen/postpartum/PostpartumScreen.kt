package com.dadnavigator.app.presentation.screen.postpartum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.SupportAgent
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
fun PostpartumScreen(onBack: () -> Unit) {
    val father = stringArrayResource(id = R.array.postpartum_father_actions).toList()
    val mother = stringArrayResource(id = R.array.postpartum_mother_observe).toList()
    val baby = stringArrayResource(id = R.array.postpartum_baby_observe).toList()
    val doctor = stringArrayResource(id = R.array.postpartum_doctor_signs).toList()

    ScreenScaffold(
        title = stringResource(id = R.string.postpartum_title),
        subtitle = stringResource(id = R.string.postpartum_subtitle),
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
                        title = stringResource(id = R.string.postpartum_for_father),
                        lines = father,
                        icon = Icons.Outlined.SupportAgent
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.postpartum_for_mother),
                        lines = mother,
                        icon = Icons.Outlined.HealthAndSafety
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.postpartum_for_baby),
                        lines = baby,
                        icon = Icons.Outlined.ChildCare
                    )
                }
                item {
                    InfoSectionCard(
                        title = stringResource(id = R.string.postpartum_medical),
                        lines = doctor,
                        icon = Icons.Outlined.BabyChangingStation
                    )
                }
            }
        }
    }
}
