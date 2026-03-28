package com.dadnavigator.app.presentation.screen.contraction

import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.service.ActiveLaborRecommendation
import com.dadnavigator.app.domain.service.ActiveLaborRecommendationSource
import java.time.Duration

/**
 * UI state for the active labor screen.
 */
data class ContractionUiState(
    val isSessionActive: Boolean = false,
    val sessionId: Long? = null,
    val activeContractionId: Long? = null,
    val sessionDuration: Duration = Duration.ZERO,
    val currentContractionDuration: Duration = Duration.ZERO,
    val currentInterval: Duration? = null,
    val stats: ContractionStats = ContractionStats(
        count = 0,
        averageDuration = null,
        averageInterval = null,
        lastInterval = null,
        trend = ContractionTrend.INSUFFICIENT_DATA,
        recommendationLevel = RecommendationLevel.MONITOR
    ),
    val recommendation: ActiveLaborRecommendation = ActiveLaborRecommendation(
        level = RecommendationLevel.MONITOR,
        source = ActiveLaborRecommendationSource.CONTRACTIONS
    ),
    val latestWaterBreak: WaterBreakEvent? = null,
    val waterBreakElapsed: Duration? = null,
    val birthRecorded: Boolean = false,
    val contractions: List<Contraction> = emptyList(),
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
