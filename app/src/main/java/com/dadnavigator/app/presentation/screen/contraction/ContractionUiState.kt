package com.dadnavigator.app.presentation.screen.contraction

import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import java.time.Duration

/**
 * UI state for contraction counter screen.
 */
data class ContractionUiState(
    val isSessionActive: Boolean = false,
    val sessionId: Long? = null,
    val activeContractionId: Long? = null,
    val sessionDuration: Duration = Duration.ZERO,
    val currentContractionDuration: Duration = Duration.ZERO,
    val stats: ContractionStats = ContractionStats(
        count = 0,
        averageDuration = null,
        averageInterval = null,
        lastInterval = null,
        trend = ContractionTrend.INSUFFICIENT_DATA,
        recommendationLevel = RecommendationLevel.MONITOR
    ),
    val contractions: List<Contraction> = emptyList(),
    val errorRes: Int? = null
)
