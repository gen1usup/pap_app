package com.dadnavigator.app.domain.usecase.labor

import com.dadnavigator.app.domain.model.TimelineType
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
    private val stageTransitionManager: StageTransitionManager,
    private val addTimelineEventUseCase: AddTimelineEventUseCase
) {
    suspend operator fun invoke(
        userId: String,
        eventTitle: String,
        eventDescription: String = ""
    ) {
        val currentSettings = settingsRepository.observeSettings().first()
        settingsRepository.saveSettings(
            currentSettings.copy(appStage = stageTransitionManager.arrivedHome())
        )
        addTimelineEventUseCase(
            userId = userId,
            timestamp = Instant.now(),
            title = eventTitle,
            description = eventDescription,
            type = TimelineType.NOTE
        )
    }
}
