package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import java.time.Duration
import javax.inject.Inject
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * Computes contraction analytics using a recent sliding window.
 *
 * The recommendation is intentionally based on recent and regular pattern quality,
 * not on the whole historical average, to reduce false positives.
 */
class CalculateContractionStatsUseCase @Inject constructor() {

    private companion object {
        val RECENT_WINDOW_DURATION: Duration = Duration.ofMinutes(90)
        const val RECENT_WINDOW_MAX_COUNT: Int = 12

        const val MIN_RECENT_CONTRACTIONS_FOR_PREPARE: Int = 4
        const val MIN_RECENT_INTERVALS_FOR_PREPARE: Int = 3

        const val MIN_RECENT_CONTRACTIONS_FOR_GO: Int = 5
        const val MIN_RECENT_INTERVALS_FOR_GO: Int = 4

        val PREPARE_MAX_INTERVAL: Duration = Duration.ofMinutes(8)
        val GO_MAX_INTERVAL: Duration = Duration.ofMinutes(5)
        val TREND_MAX_INTERVAL: Duration = Duration.ofMinutes(6)

        val PREPARE_MIN_DURATION: Duration = Duration.ofSeconds(45)
        val GO_MIN_DURATION: Duration = Duration.ofSeconds(60)

        val PREPARE_MIN_PATTERN_HOLD: Duration = Duration.ofMinutes(20)

        // Product heuristic: intentionally earlier escalation than literal "5-1-1 for one hour".
        val GO_MIN_PATTERN_HOLD: Duration = Duration.ofMinutes(30)

        const val REGULARITY_CV_PREPARE: Double = 0.50
        const val REGULARITY_CV_GO: Double = 0.35

        const val MIN_CONSECUTIVE_SHORT_INTERVALS_FOR_GO: Int = 3
        const val MIN_CONSECUTIVE_LONG_DURATIONS_FOR_GO: Int = 4
    }

    operator fun invoke(contractions: List<Contraction>): ContractionStats {
        val completed = contractions.filter { it.endedAt != null }.sortedBy { it.startedAt }
        if (completed.isEmpty()) {
            return ContractionStats(
                count = 0,
                averageDuration = null,
                averageInterval = null,
                lastInterval = null,
                trend = ContractionTrend.INSUFFICIENT_DATA,
                recommendationLevel = RecommendationLevel.MONITOR,
                recentContractionCount = 0,
                recentIntervalCount = 0,
                recentAverageDuration = null,
                recentAverageInterval = null,
                intervalStdDeviation = null,
                recentWindowSpan = Duration.ZERO,
                currentPatternHeldFor = Duration.ZERO,
                isRegularForPrepare = false,
                isRegularForGo = false
            )
        }

        val allDurations = completed.mapNotNull { it.duration }
        val allIntervals = completed.zipWithNext { previous, current ->
            Duration.between(previous.startedAt, current.startedAt)
        }

        val averageDuration = allDurations.averageDuration()
        val averageInterval = allIntervals.averageDuration()
        val lastInterval = allIntervals.lastOrNull()

        val recentContractions = selectRecentWindow(completed)
        val recentDurations = recentContractions.mapNotNull { it.duration }
        val recentIntervals = recentContractions.zipWithNext { previous, current ->
            Duration.between(previous.startedAt, current.startedAt)
        }

        val recentAverageDuration = recentDurations.averageDuration()
        val recentAverageInterval = recentIntervals.averageDuration()
        val intervalStdDeviation = recentIntervals.standardDeviation()
        val recentWindowSpan = recentContractions.spanByStart()

        val intervalCoefficientOfVariation = recentIntervals.coefficientOfVariation()
        val isRegularForPrepare = intervalCoefficientOfVariation?.let { it <= REGULARITY_CV_PREPARE } == true
        val isRegularForGo = intervalCoefficientOfVariation?.let { it <= REGULARITY_CV_GO } == true

        val currentPatternHeldFor = recentContractions.currentPatternHeldFor(
            maxInterval = PREPARE_MAX_INTERVAL,
            minDuration = PREPARE_MIN_DURATION
        )
        val goPatternHeldFor = recentContractions.currentPatternHeldFor(
            maxInterval = GO_MAX_INTERVAL,
            minDuration = GO_MIN_DURATION
        )

        val trend = computeTrend(recentIntervals, recentDurations)
        val recommendationLevel = computeRecommendation(
            recentContractionCount = recentContractions.size,
            recentIntervalCount = recentIntervals.size,
            recentAverageDuration = recentAverageDuration,
            recentAverageInterval = recentAverageInterval,
            isRegularForPrepare = isRegularForPrepare,
            isRegularForGo = isRegularForGo,
            currentPatternHeldFor = currentPatternHeldFor,
            goPatternHeldFor = goPatternHeldFor,
            trend = trend,
            consecutiveShortIntervals = recentIntervals.countConsecutiveFromEnd { it <= GO_MAX_INTERVAL },
            consecutiveLongDurations = recentDurations.countConsecutiveFromEnd { it >= GO_MIN_DURATION }
        )

        return ContractionStats(
            count = completed.size,
            averageDuration = averageDuration,
            averageInterval = averageInterval,
            lastInterval = lastInterval,
            trend = trend,
            recommendationLevel = recommendationLevel,
            recentContractionCount = recentContractions.size,
            recentIntervalCount = recentIntervals.size,
            recentAverageDuration = recentAverageDuration,
            recentAverageInterval = recentAverageInterval,
            intervalStdDeviation = intervalStdDeviation,
            recentWindowSpan = recentWindowSpan,
            currentPatternHeldFor = currentPatternHeldFor,
            isRegularForPrepare = isRegularForPrepare,
            isRegularForGo = isRegularForGo
        )
    }

    private fun selectRecentWindow(completed: List<Contraction>): List<Contraction> {
        val latestStart = completed.last().startedAt
        val minStart = latestStart.minus(RECENT_WINDOW_DURATION)

        return completed
            .filter { !it.startedAt.isBefore(minStart) }
            .takeLast(RECENT_WINDOW_MAX_COUNT)
    }

    private fun computeTrend(intervals: List<Duration>, durations: List<Duration>): ContractionTrend {
        if (intervals.size < 4 || durations.size < 4) {
            return ContractionTrend.INSUFFICIENT_DATA
        }

        val firstIntervals = intervals.take(intervals.size / 2)
        val secondIntervals = intervals.takeLast(intervals.size / 2)
        val firstDurations = durations.take(durations.size / 2)
        val secondDurations = durations.takeLast(durations.size / 2)

        val firstIntervalAvg = firstIntervals.averageDuration() ?: return ContractionTrend.INSUFFICIENT_DATA
        val secondIntervalAvg = secondIntervals.averageDuration() ?: return ContractionTrend.INSUFFICIENT_DATA
        val firstDurationAvg = firstDurations.averageDuration() ?: return ContractionTrend.INSUFFICIENT_DATA
        val secondDurationAvg = secondDurations.averageDuration() ?: return ContractionTrend.INSUFFICIENT_DATA

        return when {
            secondIntervalAvg < firstIntervalAvg && secondDurationAvg > firstDurationAvg -> {
                ContractionTrend.BECOMING_MORE_INTENSE
            }

            secondIntervalAvg > firstIntervalAvg && secondDurationAvg < firstDurationAvg -> {
                ContractionTrend.BECOMING_WEAKER
            }

            else -> ContractionTrend.STABLE
        }
    }

    private fun computeRecommendation(
        recentContractionCount: Int,
        recentIntervalCount: Int,
        recentAverageDuration: Duration?,
        recentAverageInterval: Duration?,
        isRegularForPrepare: Boolean,
        isRegularForGo: Boolean,
        currentPatternHeldFor: Duration,
        goPatternHeldFor: Duration,
        trend: ContractionTrend,
        consecutiveShortIntervals: Int,
        consecutiveLongDurations: Int
    ): RecommendationLevel {
        if (
            recentContractionCount < MIN_RECENT_CONTRACTIONS_FOR_PREPARE ||
            recentIntervalCount < MIN_RECENT_INTERVALS_FOR_PREPARE ||
            recentAverageDuration == null ||
            recentAverageInterval == null
        ) {
            return RecommendationLevel.MONITOR
        }

        val qualifiesForGo =
            recentContractionCount >= MIN_RECENT_CONTRACTIONS_FOR_GO &&
                recentIntervalCount >= MIN_RECENT_INTERVALS_FOR_GO &&
                recentAverageInterval <= GO_MAX_INTERVAL &&
                recentAverageDuration >= GO_MIN_DURATION &&
                goPatternHeldFor >= GO_MIN_PATTERN_HOLD &&
                isRegularForGo &&
                consecutiveShortIntervals >= MIN_CONSECUTIVE_SHORT_INTERVALS_FOR_GO &&
                consecutiveLongDurations >= MIN_CONSECUTIVE_LONG_DURATIONS_FOR_GO

        if (qualifiesForGo) {
            return RecommendationLevel.GO_TO_HOSPITAL
        }

        val qualifiesForPrepare =
            recentAverageInterval <= PREPARE_MAX_INTERVAL &&
                recentAverageDuration >= PREPARE_MIN_DURATION &&
                currentPatternHeldFor >= PREPARE_MIN_PATTERN_HOLD &&
                isRegularForPrepare

        if (qualifiesForPrepare) {
            return RecommendationLevel.PREPARE
        }

        // Trend is treated as secondary hint and cannot trigger GO on its own.
        if (
            trend == ContractionTrend.BECOMING_MORE_INTENSE &&
            recentAverageInterval <= TREND_MAX_INTERVAL &&
            recentAverageDuration >= PREPARE_MIN_DURATION &&
            currentPatternHeldFor >= PREPARE_MIN_PATTERN_HOLD &&
            isRegularForPrepare
        ) {
            return RecommendationLevel.PREPARE
        }

        return RecommendationLevel.MONITOR
    }

    private fun List<Contraction>.spanByStart(): Duration {
        if (size < 2) return Duration.ZERO
        return Duration.between(first().startedAt, last().startedAt)
    }

    /**
     * Duration of the latest continuous suffix that satisfies duration and interval limits.
     */
    private fun List<Contraction>.currentPatternHeldFor(
        maxInterval: Duration,
        minDuration: Duration
    ): Duration {
        if (size < 2) return Duration.ZERO

        val lastDuration = last().duration ?: return Duration.ZERO
        if (lastDuration < minDuration) return Duration.ZERO

        var streakStartIndex = lastIndex

        for (index in lastIndex downTo 1) {
            val current = this[index]
            val previous = this[index - 1]

            val currentDuration = current.duration ?: break
            val previousDuration = previous.duration ?: break
            val interval = Duration.between(previous.startedAt, current.startedAt)

            if (
                currentDuration >= minDuration &&
                previousDuration >= minDuration &&
                interval <= maxInterval
            ) {
                streakStartIndex = index - 1
            } else {
                break
            }
        }

        if (streakStartIndex == lastIndex) return Duration.ZERO
        return Duration.between(this[streakStartIndex].startedAt, last().startedAt)
    }

    private fun List<Duration>.averageDuration(): Duration? {
        if (isEmpty()) return null
        val totalMillis = sumOf { it.toMillis() }
        return Duration.ofMillis(totalMillis / size)
    }

    private fun List<Duration>.standardDeviation(): Duration? {
        if (size < 2) return null

        val values = map { it.toMillis().toDouble() }
        val mean = values.average()
        val variance = values.map { value ->
            val delta = value - mean
            delta * delta
        }.average()

        return Duration.ofMillis(sqrt(variance).roundToLong())
    }

    private fun List<Duration>.coefficientOfVariation(): Double? {
        if (size < 2) return null

        val values = map { it.toMillis().toDouble() }
        val mean = values.average()
        if (mean <= 0.0) return null

        val variance = values.map { value ->
            val delta = value - mean
            delta * delta
        }.average()
        val stdDev = sqrt(variance)

        return stdDev / mean
    }

    private fun <T> List<T>.countConsecutiveFromEnd(predicate: (T) -> Boolean): Int {
        var count = 0
        for (index in indices.reversed()) {
            if (predicate(this[index])) {
                count += 1
            } else {
                break
            }
        }
        return count
    }
}
