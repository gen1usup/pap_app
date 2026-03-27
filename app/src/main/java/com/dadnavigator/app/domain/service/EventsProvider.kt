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
            AppStage.CONTRACTIONS -> buildContractionsSections(
                isContractionRunning = isContractionRunning,
                hasActiveWaterBreak = hasActiveWaterBreak
            )
            AppStage.AT_HOSPITAL -> buildHospitalSections(laborSummary)
            AppStage.AT_HOME -> buildAtHomeSections()
        }

        return EventsContent(
            sections = sections,
            showBirthSummary = stageInfo.currentStage == AppStage.AT_HOSPITAL ||
                stageInfo.currentStage == AppStage.AT_HOME ||
                laborSummary.birthTime != null
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
                    EventAction.RecordTestDrive,
                    EventAction.RecordPreparationNote
                )
            )
        )
    }

    private fun buildContractionsSections(
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
                    EventAction.ShowBirthSheet,
                    EventAction.RecordLaborNote
                )
            )
        )
    }

    private fun buildHospitalSections(laborSummary: LaborSummary): List<EventsSection> {
        val firstAction = if (laborSummary.birthTime == null) {
            EventAction.ShowBirthSheet
        } else {
            EventAction.OpenBirthDetails
        }

        return listOf(
            EventsSection(
                type = EventsSectionType.HospitalActions,
                actions = listOf(
                    firstAction,
                    EventAction.RecordHospitalNote,
                    EventAction.RecordSupportAction,
                    EventAction.RecordPhotoNote,
                    EventAction.MarkArrivedHome
                )
            )
        )
    }

    private fun buildAtHomeSections(): List<EventsSection> {
        return listOf(
            EventsSection(
                type = EventsSectionType.HomeTrackers,
                actions = listOf(
                    EventAction.RecordFeeding,
                    EventAction.RecordSleep,
                    EventAction.RecordDiaper
                )
            ),
            EventsSection(
                type = EventsSectionType.HomeNotes,
                actions = listOf(
                    EventAction.RecordTemperature,
                    EventAction.RecordWeight,
                    EventAction.RecordHomeNote
                )
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
    HospitalActions,
    HomeTrackers,
    HomeNotes
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
    MarkArrivedHome,
    RecordBagReady,
    RecordTestDrive,
    RecordPreparationNote,
    RecordLaborNote,
    RecordHospitalNote,
    RecordSupportAction,
    RecordPhotoNote,
    RecordFeeding,
    RecordSleep,
    RecordDiaper,
    RecordTemperature,
    RecordWeight,
    RecordHomeNote
}
