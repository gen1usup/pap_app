package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveLaborRecommendationPolicyTest {

    private val policy = ActiveLaborRecommendationPolicy()

    @Test
    fun `birth recorded has the highest priority`() {
        val result = policy.resolve(
            contractionStats = stats(RecommendationLevel.GO_TO_HOSPITAL),
            latestWaterBreak = waterBreak(WaterColor.GREEN),
            birthRecorded = true
        )

        assertEquals(RecommendationLevel.MONITOR, result.level)
        assertEquals(ActiveLaborRecommendationSource.BIRTH_RECORDED, result.source)
    }

    @Test
    fun `non clear water escalates to emergency`() {
        val result = policy.resolve(
            contractionStats = stats(RecommendationLevel.MONITOR),
            latestWaterBreak = waterBreak(WaterColor.BROWN),
            birthRecorded = false
        )

        assertEquals(RecommendationLevel.EMERGENCY, result.level)
        assertEquals(ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR, result.source)
    }

    @Test
    fun `clear water prompts preparation contact`() {
        val result = policy.resolve(
            contractionStats = stats(RecommendationLevel.MONITOR),
            latestWaterBreak = waterBreak(WaterColor.CLEAR),
            birthRecorded = false
        )

        assertEquals(RecommendationLevel.PREPARE, result.level)
        assertEquals(ActiveLaborRecommendationSource.WATER_BREAK_CLEAR, result.source)
    }

    @Test
    fun `falls back to contraction recommendation when no water break`() {
        val result = policy.resolve(
            contractionStats = stats(RecommendationLevel.GO_TO_HOSPITAL),
            latestWaterBreak = null,
            birthRecorded = false
        )

        assertEquals(RecommendationLevel.GO_TO_HOSPITAL, result.level)
        assertEquals(ActiveLaborRecommendationSource.CONTRACTIONS, result.source)
    }

    private fun stats(level: RecommendationLevel): ContractionStats = ContractionStats(
        count = 0,
        averageDuration = Duration.ZERO,
        averageInterval = Duration.ZERO,
        lastInterval = Duration.ZERO,
        trend = ContractionTrend.INSUFFICIENT_DATA,
        recommendationLevel = level
    )

    private fun waterBreak(color: WaterColor): WaterBreakEvent = WaterBreakEvent(
        id = 1L,
        userId = "user",
        happenedAt = Instant.parse("2026-03-28T10:00:00Z"),
        color = color,
        notes = "",
        closedAt = null
    )
}
