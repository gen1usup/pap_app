package com.dadnavigator.app.domain.usecase.waterbreak

import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams all water break records.
 */
class ObserveWaterBreakHistoryUseCase @Inject constructor(
    private val waterBreakRepository: WaterBreakRepository
) {
    operator fun invoke(userId: String): Flow<List<WaterBreakEvent>> =
        waterBreakRepository.observeHistory(userId)
}
