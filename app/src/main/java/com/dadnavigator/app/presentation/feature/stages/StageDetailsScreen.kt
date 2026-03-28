package com.dadnavigator.app.presentation.feature.stages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dadnavigator.app.R
import com.dadnavigator.app.core.ui.DadTheme
import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.presentation.component.InfoCard
import com.dadnavigator.app.presentation.component.PrimaryButton
import com.dadnavigator.app.presentation.component.ScreenBackground
import com.dadnavigator.app.presentation.component.ScreenScaffold
import com.dadnavigator.app.presentation.component.SecondaryButton
import com.dadnavigator.app.presentation.component.StatusCard
import com.dadnavigator.app.presentation.component.StatusTone
import com.dadnavigator.app.presentation.navigation.stageLabelRes

@Composable
fun StageDetailsScreen(
    stage: AppStage,
    currentStage: AppStage,
    birthRecorded: Boolean,
    onBack: () -> Unit,
    onActivate: (AppStage) -> Unit
) {
    val spacing = DadTheme.spacing
    val isCurrentStage = stage == currentStage

    ScreenScaffold(
        title = stringResource(id = stageLabelRes(stage)),
        subtitle = stringResource(id = R.string.stage_screen_subtitle),
        onBack = onBack
    ) { innerPadding ->
        ScreenBackground {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                item {
                    StatusCard(
                        title = stringResource(id = stageTitleRes(stage)),
                        description = stringResource(id = stageDescriptionRes(stage)),
                        tone = when (stage) {
                            AppStage.PREPARING -> StatusTone.Calm
                            AppStage.CONTRACTIONS -> StatusTone.Warning
                            AppStage.AT_HOSPITAL,
                            AppStage.AT_HOME -> StatusTone.Success
                        },
                        icon = stageIcon(stage),
                        headline = stringResource(id = R.string.stage_screen_overline)
                    ) {
                        if (isCurrentStage) {
                            SecondaryButton(
                                text = stringResource(id = R.string.stage_screen_active),
                                onClick = {},
                                enabled = false
                            )
                        } else {
                            PrimaryButton(
                                text = stringResource(id = R.string.stage_screen_activate),
                                onClick = { onActivate(stage) },
                                icon = Icons.Outlined.Route
                            )
                        }
                    }
                }

                item {
                    InfoCard(
                        title = stringResource(id = R.string.stage_screen_visibility_title),
                        description = stringResource(id = stageVisibilityRes(stage)),
                        icon = stageIcon(stage)
                    )
                }

                item {
                    Text(
                        text = stringResource(id = R.string.stage_screen_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun stageTitleRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.stage_screen_preparing_title
    AppStage.CONTRACTIONS -> R.string.stage_screen_contractions_title
    AppStage.AT_HOSPITAL -> R.string.stage_screen_at_hospital_title
    AppStage.AT_HOME -> R.string.stage_screen_at_home_title
}

private fun stageDescriptionRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.stage_screen_preparing_description
    AppStage.CONTRACTIONS -> R.string.stage_screen_contractions_description
    AppStage.AT_HOSPITAL -> R.string.stage_screen_at_hospital_description
    AppStage.AT_HOME -> R.string.stage_screen_at_home_description
}

private fun stageVisibilityRes(stage: AppStage): Int = when (stage) {
    AppStage.PREPARING -> R.string.stage_screen_preparing_visibility
    AppStage.CONTRACTIONS -> R.string.stage_screen_contractions_visibility
    AppStage.AT_HOSPITAL -> R.string.stage_screen_at_hospital_visibility
    AppStage.AT_HOME -> R.string.stage_screen_at_home_visibility
}

private fun stageIcon(stage: AppStage): ImageVector = when (stage) {
    AppStage.PREPARING -> Icons.Outlined.Checklist
    AppStage.CONTRACTIONS -> Icons.Outlined.MonitorHeart
    AppStage.AT_HOSPITAL -> Icons.Outlined.LocalHospital
    AppStage.AT_HOME -> Icons.Outlined.ChildCare
}
