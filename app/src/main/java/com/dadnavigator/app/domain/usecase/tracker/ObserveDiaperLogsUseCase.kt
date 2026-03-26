package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.model.DiaperLog
import com.dadnavigator.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams diaper logs.
 */
class ObserveDiaperLogsUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    operator fun invoke(userId: String): Flow<List<DiaperLog>> = trackerRepository.observeDiaperLogs(userId)
}
