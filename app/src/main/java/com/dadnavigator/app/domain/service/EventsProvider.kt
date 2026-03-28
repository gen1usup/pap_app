package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.StageInfo
import javax.inject.Inject

/**
 * Builds the Events screen structure from the current stage and saved milestones.
 *
 * The provider exposes semantic sections and actions only. UI strings and icons
 * stay in the presentation layer.
 */
class EventsProvider @Inject constructor() {

    fun build(
        stageInfo: StageInfo,
        isContractionRunning: Boolean,
        hasActiveWaterBreak: Boolean,
        laborSummary: LaborSummary
    ): EventsContent {
        val sections = when (stageInfo.currentStage) {
            AppStage.PREPARING -> buildPreparationSections(stageInfo)
            AppStage.LABOR -> buildLaborSections(
                isContractionRunning = isContractionRunning,
                hasActiveWaterBreak = hasActiveWaterBreak
            )
            AppStage.BABY_BORN -> buildBabyBornSections(hasActiveWaterBreak)
        }

        return EventsContent(
            sections = sections,
            showBirthSummary = stageInfo.currentStage == AppStage.BABY_BORN || laborSummary.birthTime != null
        )
    }

    private fun buildPreparationSections(stageInfo: StageInfo): List<EventsSection> {
        val readinessActions = buildList {
            if (stageInfo.isLaborReadinessWindow) {
                add(EventAction.MarkLaborStarted)
            }
            add(EventAction.OpenContractionTimer)
            add(EventAction.OpenDecisionHelp)
            if (stageInfo.isLaborReadinessWindow) {
                add(EventAction.OpenWaterBreakTimer)
            }
        }

        return listOf(
            EventsSection(
                type = if (stageInfo.isLaborReadinessWindow) {
                    EventsSectionType.ReadinessWindow
                } else {
                    EventsSectionType.PreparationTools
                },
                actions = readinessActions
            ),
            EventsSection(
                type = EventsSectionType.PreparationRecords,
                actions = listOf(
                    EventAction.RecordBagReady,
                    EventAction.RecordTestDrive
                )
            )
        )
    }

    private fun buildLaborSections(
        isContractionRunning: Boolean,
        hasActiveWaterBreak: Boolean
    ): List<EventsSection> {
        return listOf(
            EventsSection(
                type = EventsSectionType.LiveLaborActions,
                actions = buildList {
                    add(
                        if (isContractionRunning) {
                            EventAction.StopContraction
                        } else {
                            EventAction.StartContraction
                        }
                    )
                    add(EventAction.OpenContractionTimer)
                    add(
                        if (hasActiveWaterBreak) {
                            EventAction.OpenWaterBreakTimer
                        } else {
                            EventAction.OpenWaterBreakTimer
                        }
                    )
                    add(EventAction.OpenDecisionHelp)
                }
            ),
            EventsSection(
                type = EventsSectionType.LaborLogistics,
                actions = listOf(
                    EventAction.MarkLeftHome,
                    EventAction.MarkArrivedHospital,
                    EventAction.ShowBirthSheet
                )
            )
        )
    }

    private fun buildBabyBornSections(
        hasActiveWaterBreak: Boolean
    ): List<EventsSection> {
        return listOf(
            EventsSection(
                type = EventsSectionType.BabyBornActions,
                actions = buildList {
                    add(EventAction.OpenBirthDetails)
                    if (hasActiveWaterBreak) {
                        add(EventAction.OpenWaterBreakTimer)
                    }
                    add(EventAction.RecordSupportAction)
                    add(EventAction.RecordPhotoNote)
                    add(EventAction.RecordFeeding)
                    add(EventAction.RecordSleep)
                    add(EventAction.RecordDiaper)
                    add(EventAction.RecordTemperature)
                    add(EventAction.RecordWeight)
                }
            )
        )
    }
}

data class EventsContent(
    val sections: List<EventsSection>,
    val showBirthSummary: Boolean
)

data class EventsSection(
    val type: EventsSectionType,
    val actions: List<EventAction>
)

enum class EventsSectionType {
    PreparationTools,
    ReadinessWindow,
    PreparationRecords,
    LiveLaborActions,
    LaborLogistics,
    BabyBornActions
}

enum class EventAction {
    OpenContractionTimer,
    OpenWaterBreakTimer,
    OpenDecisionHelp,
    OpenBirthDetails,
    MarkLaborStarted,
    StartContraction,
    StopContraction,
    MarkLeftHome,
    MarkArrivedHospital,
    ShowBirthSheet,
    RecordBagReady,
    RecordTestDrive,
    RecordPreparationNote,
    RecordLaborNote,
    RecordBabyNote,
    RecordSupportAction,
    RecordPhotoNote,
    RecordFeeding,
    RecordSleep,
    RecordDiaper,
    RecordTemperature,
    RecordWeight
}
