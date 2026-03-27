package com.dadnavigator.app.domain.model

import java.time.LocalDate

/**
 * Derived stage context used by orchestrators and stage-aware features.
 */
data class StageInfo(
    val currentStage: AppStage,
    val dueDate: LocalDate?,
    val daysUntilDueDate: Long?,
    val estimatedPregnancyWeek: Int?,
    val isDueDateMissing: Boolean,
    val isLaborReadinessWindow: Boolean,
    val birthRecorded: Boolean
)
