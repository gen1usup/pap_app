package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.repository.TrackerRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Adds free-form note.
 */
class AddNoteUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    suspend operator fun invoke(userId: String, timestamp: Instant, text: String, category: String) {
        if (text.isBlank()) return
        trackerRepository.addNote(userId, timestamp, text.trim(), category)
    }
}
