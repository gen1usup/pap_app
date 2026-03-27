package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.StageInfo
import javax.inject.Inject

/**
 * Builds the high-level home configuration from stage and context so
 * composables can stay focused on rendering.
 */
class HomeContentBuilder @Inject constructor() {

    fun build(
        settings: Settings,
        stageInfo: StageInfo,
        laborSummary: LaborSummary,
        hasActiveContractionSession: Boolean,
        hasActiveWaterBreak: Boolean
    ): HomeContent = HomeContent(
        showDueDateReminder = stageInfo.isDueDateMissing,
        showDueDateCard = !stageInfo.isDueDateMissing,
        showContractionShortcut = when (settings.appStage) {
            AppStage.PREPARING -> hasActiveContractionSession || stageInfo.isLaborReadinessWindow
            AppStage.CONTRACTIONS -> true
            AppStage.AT_HOSPITAL,
            AppStage.AT_HOME -> false
        },
        showLiveContractionBlock = settings.appStage == AppStage.CONTRACTIONS &&
            laborSummary.birthTime == null,
        showWaterBreakShortcut = settings.appStage == AppStage.CONTRACTIONS || hasActiveWaterBreak,
        showBirthDetailsCard = settings.appStage == AppStage.AT_HOSPITAL &&
            (laborSummary.babyName.isNullOrBlank() ||
                laborSummary.birthWeightGrams == null ||
                laborSummary.birthHeightCm == null),
        checklistFirst = settings.appStage == AppStage.PREPARING,
        showLaborQuickActions = settings.appStage == AppStage.CONTRACTIONS &&
            laborSummary.birthTime == null
    )
}

data class HomeContent(
    val showDueDateReminder: Boolean,
    val showDueDateCard: Boolean,
    val showContractionShortcut: Boolean,
    val showLiveContractionBlock: Boolean,
    val showWaterBreakShortcut: Boolean,
    val showBirthDetailsCard: Boolean,
    val checklistFirst: Boolean,
    val showLaborQuickActions: Boolean
)
