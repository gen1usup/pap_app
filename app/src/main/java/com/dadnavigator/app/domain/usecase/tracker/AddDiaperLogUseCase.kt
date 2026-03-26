package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.model.DiaperType
import com.dadnavigator.app.domain.repository.TrackerRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Adds diaper change record.
 */
class AddDiaperLogUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    suspend operator fun invoke(
        userId: String,
        timestamp: Instant,
        type: DiaperType,
        notes: String
    ) {
        trackerRepository.addDiaperLog(userId, timestamp, type, notes.trim())
    }
}
