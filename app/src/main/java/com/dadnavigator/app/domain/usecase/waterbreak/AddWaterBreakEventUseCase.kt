package com.dadnavigator.app.domain.usecase.waterbreak

import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import java.time.Instant
import javax.inject.Inject

/**
 * Creates water break event.
 */
class AddWaterBreakEventUseCase @Inject constructor(
    private val waterBreakRepository: WaterBreakRepository
) {
    suspend operator fun invoke(
        userId: String,
        happenedAt: Instant,
        color: WaterColor,
        notes: String
    ) {
        waterBreakRepository.createEvent(userId, happenedAt, color, notes.trim())
    }
}
