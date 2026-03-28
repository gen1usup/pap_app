package com.dadnavigator.app.domain.usecase.timeline

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineEntryType
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.model.defaultEntryType
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.TimelineRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Adds one event to common timeline.
 */
class AddTimelineEventUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        userId: String,
        timestamp: Instant,
        title: String,
        description: String,
        type: TimelineType,
        stageAtCreation: AppStage? = null,
        entryType: TimelineEntryType = type.defaultEntryType()
    ) {
        val resolvedStage = stageAtCreation ?: settingsRepository.observeSettings().first().appStage
        timelineRepository.addEvent(
            userId = userId,
            timestamp = timestamp,
            title = title.trim(),
            description = description.trim(),
            type = type,
            stageAtCreation = resolvedStage,
            entryType = entryType
        )
    }
}
