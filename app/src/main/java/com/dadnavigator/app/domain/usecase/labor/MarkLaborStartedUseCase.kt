package com.dadnavigator.app.domain.usecase.labor

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.TimelineRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Marks the start of labor and switches the app into labor mode.
 */
class MarkLaborStartedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val laborRepository: LaborRepository,
    private val timelineRepository: TimelineRepository
) {
    suspend operator fun invoke(
        userId: String,
        eventTitle: String,
        eventDescription: String = "",
        timestamp: Instant = Instant.now()
    ) {
        val settings = settingsRepository.observeSettings().first()
        settingsRepository.saveSettings(settings.copy(appStage = AppStage.LABOR))

        val currentSummary = laborRepository.observeLaborSummary(userId).first()
        if (currentSummary.laborStartTime == null) {
            laborRepository.saveLaborSummary(
                userId = userId,
                summary = currentSummary.copy(laborStartTime = timestamp)
            )
            timelineRepository.addEvent(
                userId = userId,
                timestamp = timestamp,
                title = eventTitle.trim(),
                description = eventDescription.trim(),
                type = TimelineType.LABOR
            )
        }
    }
}
