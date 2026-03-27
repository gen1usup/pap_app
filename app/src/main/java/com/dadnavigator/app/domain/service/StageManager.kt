package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.model.Settings
import com.dadnavigator.app.domain.model.StageInfo
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Builds derived stage context from manual app stage and saved milestones.
 */
class StageManager @Inject constructor() {

    fun buildStageInfo(
        settings: Settings,
        laborSummary: LaborSummary,
        today: LocalDate = LocalDate.now()
    ): StageInfo {
        val daysUntilDueDate = settings.dueDate?.let { dueDate ->
            ChronoUnit.DAYS.between(today, dueDate)
        }
        val estimatedPregnancyWeek = daysUntilDueDate?.let { daysUntilDue ->
            (((280L - daysUntilDue).coerceAtLeast(0L)) / 7L).toInt()
        }

        return StageInfo(
            currentStage = settings.appStage,
            dueDate = settings.dueDate,
            daysUntilDueDate = daysUntilDueDate,
            estimatedPregnancyWeek = estimatedPregnancyWeek,
            isDueDateMissing = settings.dueDate == null,
            isLaborReadinessWindow = estimatedPregnancyWeek?.let { it >= 37 } == true,
            birthRecorded = laborSummary.birthTime != null
        )
    }
}
