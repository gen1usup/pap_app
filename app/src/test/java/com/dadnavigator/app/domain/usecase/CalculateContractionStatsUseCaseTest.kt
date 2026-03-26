package com.dadnavigator.app.domain.usecase

import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.usecase.contraction.CalculateContractionStatsUseCase
import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for contraction analytics use case.
 */
class CalculateContractionStatsUseCaseTest {

    private val useCase = CalculateContractionStatsUseCase()

    @Test
    fun `returns monitor for empty history`() {
        val result = useCase(emptyList())

        assertEquals(0, result.count)
        assertNull(result.averageDuration)
        assertEquals(RecommendationLevel.MONITOR, result.recommendationLevel)
    }

    @Test
    fun `returns prepare for sustained moderate regular pattern`() {
        val base = Instant.parse("2026-03-22T10:00:00Z")
        val contractions = listOf(
            contraction(1, base, 50),
            contraction(2, base.plusSeconds(7 * 60), 52),
            contraction(3, base.plusSeconds(14 * 60), 50),
            contraction(4, base.plusSeconds(21 * 60), 49),
            contraction(5, base.plusSeconds(28 * 60), 51)
        )

        val result = useCase(contractions)

        assertEquals(5, result.recentContractionCount)
        assertEquals(4, result.recentIntervalCount)
        assertTrue(result.isRegularForPrepare)
        assertTrue(result.isRegularForGo)
        assertEquals(RecommendationLevel.PREPARE, result.recommendationLevel)
    }

    @Test
    fun `returns go to hospital for sustained strong regular pattern`() {
        val base = Instant.parse("2026-03-22T10:00:00Z")
        val contractions = listOf(
            contraction(1, base, 70),
            contraction(2, base.plusSeconds(5 * 60), 72),
            contraction(3, base.plusSeconds(10 * 60), 75),
            contraction(4, base.plusSeconds(15 * 60), 70),
            contraction(5, base.plusSeconds(20 * 60), 74),
            contraction(6, base.plusSeconds(25 * 60), 76),
            contraction(7, base.plusSeconds(30 * 60), 73)
        )

        val result = useCase(contractions)

        assertEquals(7, result.recentContractionCount)
        assertTrue(result.isRegularForPrepare)
        assertTrue(result.isRegularForGo)
        assertTrue(result.currentPatternHeldFor >= Duration.ofMinutes(30))
        assertEquals(RecommendationLevel.GO_TO_HOSPITAL, result.recommendationLevel)
    }

    @Test
    fun `keeps monitor for irregular intervals even with alarming average`() {
        val base = Instant.parse("2026-03-22T10:00:00Z")
        val contractions = listOf(
            contraction(1, base, 70),
            contraction(2, base.plusSeconds(12 * 60), 70),
            contraction(3, base.plusSeconds(14 * 60), 70),
            contraction(4, base.plusSeconds(26 * 60), 70),
            contraction(5, base.plusSeconds(28 * 60), 70),
            contraction(6, base.plusSeconds(40 * 60), 70)
        )

        val result = useCase(contractions)

        assertFalse(result.isRegularForPrepare)
        assertFalse(result.isRegularForGo)
        assertEquals(RecommendationLevel.MONITOR, result.recommendationLevel)
    }

    @Test
    fun `uses recent window instead of whole history for recommendation`() {
        val base = Instant.parse("2026-03-22T10:00:00Z")
        val old = listOf(
            contraction(1, base.minusSeconds(8 * 60 * 60), 30),
            contraction(2, base.minusSeconds(7 * 60 * 60 + 40 * 60), 35),
            contraction(3, base.minusSeconds(7 * 60 * 60 + 20 * 60), 40)
        )
        val recent = listOf(
            contraction(4, base, 70),
            contraction(5, base.plusSeconds(5 * 60), 70),
            contraction(6, base.plusSeconds(10 * 60), 72),
            contraction(7, base.plusSeconds(15 * 60), 73),
            contraction(8, base.plusSeconds(20 * 60), 74),
            contraction(9, base.plusSeconds(25 * 60), 75),
            contraction(10, base.plusSeconds(30 * 60), 72)
        )

        val result = useCase(old + recent)

        assertEquals(10, result.count)
        assertEquals(7, result.recentContractionCount)
        assertEquals(RecommendationLevel.GO_TO_HOSPITAL, result.recommendationLevel)
    }

    @Test
    fun `current pattern held for is based on trailing continuous series`() {
        val base = Instant.parse("2026-03-22T10:00:00Z")
        val contractions = listOf(
            contraction(1, base, 20),
            contraction(2, base.plusSeconds(20 * 60), 30),
            contraction(3, base.plusSeconds(40 * 60), 50),
            contraction(4, base.plusSeconds(47 * 60), 52),
            contraction(5, base.plusSeconds(54 * 60), 50),
            contraction(6, base.plusSeconds(61 * 60), 51)
        )

        val result = useCase(contractions)

        assertEquals(Duration.ofMinutes(61), result.recentWindowSpan)
        assertEquals(Duration.ofMinutes(21), result.currentPatternHeldFor)
    }

    private fun contraction(id: Long, start: Instant, durationSeconds: Long): Contraction {
        return Contraction(
            id = id,
            sessionId = 1,
            userId = "user",
            startedAt = start,
            endedAt = start.plusSeconds(durationSeconds)
        )
    }
}


