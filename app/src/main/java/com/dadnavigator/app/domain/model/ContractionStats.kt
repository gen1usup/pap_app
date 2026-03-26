package com.dadnavigator.app.domain.model

import java.time.Duration

/**
 * Trend of contraction dynamics.
 */
enum class ContractionTrend {
    STABLE,
    BECOMING_MORE_INTENSE,
    BECOMING_WEAKER,
    INSUFFICIENT_DATA
}

/**
 * Recommendation level used for action prompts.
 */
enum class RecommendationLevel {
    MONITOR,
    PREPARE,
    GO_TO_HOSPITAL,
    EMERGENCY
}

/**
 * Aggregated contraction analytics for UI.
 */
data class ContractionStats(
    val count: Int,
    val averageDuration: Duration?,
    val averageInterval: Duration?,
    val lastInterval: Duration?,
    val trend: ContractionTrend,
    val recommendationLevel: RecommendationLevel,
    val recentContractionCount: Int = 0,
    val recentIntervalCount: Int = 0,
    val recentAverageDuration: Duration? = null,
    val recentAverageInterval: Duration? = null,
    val intervalStdDeviation: Duration? = null,
    val recentWindowSpan: Duration = Duration.ZERO,
    val currentPatternHeldFor: Duration = Duration.ZERO,
    val isRegularForPrepare: Boolean = false,
    val isRegularForGo: Boolean = false
)
