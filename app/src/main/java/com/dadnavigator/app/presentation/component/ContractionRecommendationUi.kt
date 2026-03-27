package com.dadnavigator.app.presentation.component

import com.dadnavigator.app.R
import com.dadnavigator.app.domain.model.RecommendationLevel

internal fun recommendationTextRes(level: RecommendationLevel): Int = when (level) {
    RecommendationLevel.MONITOR -> R.string.recommendation_monitor
    RecommendationLevel.PREPARE -> R.string.recommendation_prepare
    RecommendationLevel.GO_TO_HOSPITAL -> R.string.recommendation_go_hospital
    RecommendationLevel.EMERGENCY -> R.string.recommendation_emergency
}

internal fun recommendationHeadlineRes(level: RecommendationLevel): Int = when (level) {
    RecommendationLevel.MONITOR -> R.string.contraction_stage_monitor
    RecommendationLevel.PREPARE -> R.string.contraction_stage_prepare
    RecommendationLevel.GO_TO_HOSPITAL -> R.string.contraction_stage_go
    RecommendationLevel.EMERGENCY -> R.string.contraction_stage_emergency
}

internal fun recommendationTone(level: RecommendationLevel): StatusTone = when (level) {
    RecommendationLevel.MONITOR -> StatusTone.Calm
    RecommendationLevel.PREPARE -> StatusTone.Warning
    RecommendationLevel.GO_TO_HOSPITAL -> StatusTone.Warning
    RecommendationLevel.EMERGENCY -> StatusTone.Critical
}
