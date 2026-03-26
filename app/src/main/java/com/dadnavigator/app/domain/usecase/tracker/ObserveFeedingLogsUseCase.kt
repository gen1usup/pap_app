package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.model.FeedingLog
import com.dadnavigator.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams feeding logs.
 */
class ObserveFeedingLogsUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    operator fun invoke(userId: String): Flow<List<FeedingLog>> = trackerRepository.observeFeedingLogs(userId)
}
