package com.dadnavigator.app.domain.usecase.timeline

import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.repository.TimelineRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Adds one event to common timeline.
 */
class AddTimelineEventUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {
    suspend operator fun invoke(
        userId: String,
        timestamp: Instant,
        title: String,
        description: String,
        type: TimelineType
    ) {
        timelineRepository.addEvent(
            userId = userId,
            timestamp = timestamp,
            title = title.trim(),
            description = description.trim(),
            type = type
        )
    }
}
