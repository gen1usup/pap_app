package com.dadnavigator.app.domain.usecase.waterbreak

import com.dadnavigator.app.domain.repository.WaterBreakRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Marks active water break event as closed.
 */
class CloseWaterBreakEventUseCase @Inject constructor(
    private val waterBreakRepository: WaterBreakRepository
) {
    suspend operator fun invoke(userId: String, closedAt: Instant = Instant.now()) {
        waterBreakRepository.closeActiveEvent(userId, closedAt)
    }
}
