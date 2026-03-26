package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.model.FeedingType
import com.dadnavigator.app.domain.repository.TrackerRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Adds feeding record.
 */
class AddFeedingLogUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    suspend operator fun invoke(
        userId: String,
        timestamp: Instant,
        durationMinutes: Int,
        type: FeedingType
    ) {
        if (durationMinutes <= 0) return
        trackerRepository.addFeedingLog(userId, timestamp, durationMinutes, type)
    }
}
