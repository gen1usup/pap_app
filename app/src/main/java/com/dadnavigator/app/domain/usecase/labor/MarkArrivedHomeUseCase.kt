package com.dadnavigator.app.domain.usecase.labor

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Marks the moment when the family arrives home after the hospital.
 */
class MarkArrivedHomeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val laborRepository: LaborRepository,
    private val stageTransitionManager: StageTransitionManager,
    private val addTimelineEventUseCase: AddTimelineEventUseCase
) {
    suspend operator fun invoke(
        userId: String,
        eventTitle: String,
        eventDescription: String = ""
    ): MarkArrivedHomeResult {
        val currentSettings = settingsRepository.observeSettings().first()
        val currentSummary = laborRepository.observeLaborSummary(userId).first()

        if (currentSummary.birthTime == null) {
            return MarkArrivedHomeResult.BirthNotRecorded
        }
        if (currentSettings.appStage == AppStage.AT_HOME) {
            return MarkArrivedHomeResult.AlreadyHome
        }
        if (currentSettings.appStage != AppStage.AT_HOSPITAL) {
            return MarkArrivedHomeResult.NotAtHospital
        }

        settingsRepository.saveSettings(
            currentSettings.copy(appStage = stageTransitionManager.arrivedHome())
        )
        addTimelineEventUseCase(
            userId = userId,
            timestamp = Instant.now(),
            title = eventTitle,
            description = eventDescription,
            type = TimelineType.HOME_NOTE
        )
        return MarkArrivedHomeResult.Marked
    }
}

enum class MarkArrivedHomeResult {
    Marked,
    AlreadyHome,
    BirthNotRecorded,
    NotAtHospital
}
