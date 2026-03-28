package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import javax.inject.Inject

/**
 * Resolves the final active labor recommendation with water-break priority.
 *
 * Priority order:
 * 1. Birth already recorded.
 * 2. Non-clear water break.
 * 3. Clear or pink water break.
 * 4. Existing contraction recommendation.
 */
class ActiveLaborRecommendationPolicy @Inject constructor() {

    fun resolve(
        contractionStats: ContractionStats,
        latestWaterBreak: WaterBreakEvent?,
        birthRecorded: Boolean
    ): ActiveLaborRecommendation {
        return when {
            birthRecorded -> ActiveLaborRecommendation(
                level = RecommendationLevel.MONITOR,
                source = ActiveLaborRecommendationSource.BIRTH_RECORDED
            )

            latestWaterBreak != null && latestWaterBreak.color.isNonClear() -> ActiveLaborRecommendation(
                level = RecommendationLevel.EMERGENCY,
                source = ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR
            )

            latestWaterBreak != null -> ActiveLaborRecommendation(
                level = RecommendationLevel.PREPARE,
                source = ActiveLaborRecommendationSource.WATER_BREAK_CLEAR
            )

            else -> ActiveLaborRecommendation(
                level = contractionStats.recommendationLevel,
                source = ActiveLaborRecommendationSource.CONTRACTIONS
            )
        }
    }
}

data class ActiveLaborRecommendation(
    val level: RecommendationLevel,
    val source: ActiveLaborRecommendationSource
)

enum class ActiveLaborRecommendationSource {
    CONTRACTIONS,
    WATER_BREAK_CLEAR,
    WATER_BREAK_NON_CLEAR,
    BIRTH_RECORDED
}

private fun WaterColor.isNonClear(): Boolean = when (this) {
    WaterColor.CLEAR,
    WaterColor.PINK -> false
    WaterColor.GREEN,
    WaterColor.BROWN -> true
}
