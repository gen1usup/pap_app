package com.dadnavigator.app.domain.usecase.labor

import com.dadnavigator.app.domain.model.AppStage
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.TimelineRepository
import com.dadnavigator.app.domain.service.StageTransitionManager
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Marks the birth event, updates birth details and switches the app into after-birth mode.
 */
class MarkBirthUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val laborRepository: LaborRepository,
    private val timelineRepository: TimelineRepository,
    private val stageTransitionManager: StageTransitionManager
) {
    suspend operator fun invoke(
        userId: String,
        eventTitle: String,
        eventDescription: String,
        timestamp: Instant = Instant.now(),
        babyName: String? = null,
        birthWeightGrams: Int? = null,
        birthHeightCm: Int? = null
    ) {
        val settings = settingsRepository.observeSettings().first()
        settingsRepository.saveSettings(settings.copy(appStage = stageTransitionManager.babyBorn()))

        val currentSummary = laborRepository.observeLaborSummary(userId).first()
        val updatedSummary = currentSummary.copy(
            birthTime = currentSummary.birthTime ?: timestamp,
            babyName = babyName?.takeIf { it.isNotBlank() } ?: currentSummary.babyName,
            birthWeightGrams = birthWeightGrams ?: currentSummary.birthWeightGrams,
            birthHeightCm = birthHeightCm ?: currentSummary.birthHeightCm
        )
        laborRepository.saveLaborSummary(userId, updatedSummary)

        if (currentSummary.birthTime == null) {
            timelineRepository.addEvent(
                userId = userId,
                timestamp = updatedSummary.birthTime ?: timestamp,
                title = eventTitle.trim(),
                description = eventDescription.trim(),
                type = TimelineType.BIRTH
            )
        }
    }
}
