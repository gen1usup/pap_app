package com.dadnavigator.app.domain.usecase.waterbreak

import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Creates water break event and moves the app into LABOR stage.
 */
class AddWaterBreakEventUseCase @Inject constructor(
    private val waterBreakRepository: WaterBreakRepository,
    private val settingsRepository: SettingsRepository,
    private val laborRepository: LaborRepository,
    private val stageTransitionManager: StageTransitionManager
) {
    suspend operator fun invoke(
        userId: String,
        happenedAt: Instant,
        color: WaterColor,
        notes: String
    ) {
        waterBreakRepository.createEvent(userId, happenedAt, color, notes.trim())
        val settings = settingsRepository.observeSettings().first()
        val laborSummary = laborRepository.observeLaborSummary(userId).first()
        settingsRepository.saveSettings(
            settings.copy(appStage = stageTransitionManager.laborStarted(laborSummary))
        )
    }
}
