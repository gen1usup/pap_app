package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.model.Note
import com.dadnavigator.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams free-form notes.
 */
class ObserveNotesUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    operator fun invoke(userId: String): Flow<List<Note>> = trackerRepository.observeNotes(userId)
}
