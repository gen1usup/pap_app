package com.dadnavigator.app.domain.usecase.tracker

import com.dadnavigator.app.domain.model.SleepLog
import com.dadnavigator.app.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams sleep logs.
 */
class ObserveSleepLogsUseCase @Inject constructor(
    private val trackerRepository: TrackerRepository
) {
    operator fun invoke(userId: String): Flow<List<SleepLog>> = trackerRepository.observeSleepLogs(userId)
}
