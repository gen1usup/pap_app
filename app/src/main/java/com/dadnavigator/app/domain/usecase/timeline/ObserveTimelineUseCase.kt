package com.dadnavigator.app.domain.usecase.timeline

import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes full chronology stream.
 */
class ObserveTimelineUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {
    operator fun invoke(userId: String): Flow<List<TimelineEvent>> =
        timelineRepository.observeTimeline(userId)
}
