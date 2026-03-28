package com.dadnavigator.app.domain.usecase.waterbreak

import com.dadnavigator.app.domain.repository.WaterBreakRepository
import javax.inject.Inject

class DeleteWaterBreakEventUseCase @Inject constructor(
    private val waterBreakRepository: WaterBreakRepository
) {
    suspend operator fun invoke(eventId: Long) {
        waterBreakRepository.deleteEvent(eventId)
    }
}
