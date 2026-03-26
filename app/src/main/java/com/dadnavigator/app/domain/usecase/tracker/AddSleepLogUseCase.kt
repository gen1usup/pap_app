package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.repository.TrackerRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Adds sleep period record.
 */
class AddSleepLogUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    suspend operator fun invoke(
        userId: String,
        startTime: Instant,
        endTime: Instant,
        notes: String
    ) {
        if (endTime <= startTime) return
        trackerRepository.addSleepLog(userId, startTime, endTime, notes.trim())
    }
}
