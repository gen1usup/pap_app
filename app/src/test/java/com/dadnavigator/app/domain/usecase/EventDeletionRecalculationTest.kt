package com.dadnavigator.app.domain.usecase

import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionSession
import com.dadnavigator.app.domain.model.ContractionTrend
import com.dadnavigator.app.domain.model.RecommendationLevel
import com.dadnavigator.app.domain.model.WaterBreakEvent
import com.dadnavigator.app.domain.model.WaterColor
import com.dadnavigator.app.domain.repository.ContractionRepository
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import com.dadnavigator.app.domain.service.ActiveLaborRecommendationPolicy
import com.dadnavigator.app.domain.service.ActiveLaborRecommendationSource
import com.dadnavigator.app.domain.service.LiveContractionSnapshotBuilder
import com.dadnavigator.app.domain.usecase.contraction.CalculateContractionStatsUseCase
import com.dadnavigator.app.domain.usecase.contraction.DeleteContractionUseCase
import com.dadnavigator.app.domain.usecase.waterbreak.DeleteWaterBreakEventUseCase
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EventDeletionRecalculationTest {

    private val snapshotBuilder = LiveContractionSnapshotBuilder(CalculateContractionStatsUseCase())
    private val recommendationPolicy = ActiveLaborRecommendationPolicy()

    @Test
    fun `delete contraction removes it from interval calculation`() = runBlocking {
        val repository = FakeDeleteContractionRepository(
            contractions = listOf(
                completedContraction(1, "2026-03-28T10:00:00Z", "2026-03-28T10:01:00Z"),
                completedContraction(2, "2026-03-28T10:05:00Z", "2026-03-28T10:06:00Z"),
                completedContraction(3, "2026-03-28T10:10:00Z", "2026-03-28T10:11:00Z")
            )
        )
        val deleteUseCase = DeleteContractionUseCase(repository)
        val now = Instant.parse("2026-03-28T10:15:00Z")

        val before = snapshotBuilder.build(repository.observeActiveState("user").value, now)
        deleteUseCase(2L)
        val after = snapshotBuilder.build(repository.observeActiveState("user").value, now)

        assertEquals(3, before.stats.count)
        assertEquals(2, after.stats.count)
        assertNotEquals(before.stats.averageInterval, after.stats.averageInterval)
        assertEquals(10L, after.stats.averageInterval?.toMinutes())
        assertEquals(5L, after.currentInterval?.toMinutes())
    }

    @Test
    fun `delete water break recalculates recommendation from emergency to contractions`() = runBlocking {
        val repository = FakeDeleteWaterBreakRepository(
            history = listOf(
                WaterBreakEvent(
                    id = 7L,
                    userId = "user",
                    happenedAt = Instant.parse("2026-03-28T10:00:00Z"),
                    color = WaterColor.GREEN,
                    notes = "",
                    closedAt = null
                )
            )
        )
        val deleteUseCase = DeleteWaterBreakEventUseCase(repository)
        val contractions = listOf(
            completedContraction(1, "2026-03-28T09:00:00Z", "2026-03-28T09:01:10Z"),
            completedContraction(2, "2026-03-28T09:05:00Z", "2026-03-28T09:06:10Z"),
            completedContraction(3, "2026-03-28T09:10:00Z", "2026-03-28T09:11:05Z"),
            completedContraction(4, "2026-03-28T09:15:00Z", "2026-03-28T09:16:10Z"),
            completedContraction(5, "2026-03-28T09:20:00Z", "2026-03-28T09:21:10Z"),
            completedContraction(6, "2026-03-28T09:25:00Z", "2026-03-28T09:26:15Z"),
            completedContraction(7, "2026-03-28T09:30:00Z", "2026-03-28T09:31:10Z")
        )
        val stats = CalculateContractionStatsUseCase()(contractions)

        val before = recommendationPolicy.resolve(
            contractionStats = stats,
            latestWaterBreak = repository.observeHistory("user").value.maxByOrNull { it.happenedAt },
            birthRecorded = false
        )

        deleteUseCase(7L)

        val after = recommendationPolicy.resolve(
            contractionStats = stats,
            latestWaterBreak = repository.observeHistory("user").value.maxByOrNull { it.happenedAt },
            birthRecorded = false
        )

        assertEquals(RecommendationLevel.EMERGENCY, before.level)
        assertEquals(ActiveLaborRecommendationSource.WATER_BREAK_NON_CLEAR, before.source)
        assertEquals(RecommendationLevel.GO_TO_HOSPITAL, after.level)
        assertEquals(ActiveLaborRecommendationSource.CONTRACTIONS, after.source)
    }

    private fun completedContraction(
        id: Long,
        startedAt: String,
        endedAt: String
    ): Contraction = Contraction(
        id = id,
        sessionId = 1L,
        userId = "user",
        startedAt = Instant.parse(startedAt),
        endedAt = Instant.parse(endedAt)
    )
}

private class FakeDeleteContractionRepository(
    contractions: List<Contraction>
) : ContractionRepository {
    private val state = MutableStateFlow(
        ActiveContractionState(
            session = ContractionSession(
                id = 1L,
                userId = "user",
                startedAt = contractions.first().startedAt,
                endedAt = null
            ),
            contractions = contractions,
            activeContraction = null
        )
    )

    override fun observeActiveState(userId: String): MutableStateFlow<ActiveContractionState> = state

    override fun observeSessionHistory(userId: String): Flow<List<ContractionSession>> = flowOf(listOfNotNull(state.value.session))

    override suspend fun startSession(userId: String, startedAt: Instant): Long = 1L

    override suspend fun finishSession(sessionId: Long, endedAt: Instant) = Unit

    override suspend fun startContraction(sessionId: Long, userId: String, startedAt: Instant): Long = 1L

    override suspend fun finishContraction(contractionId: Long, endedAt: Instant) = Unit

    override suspend fun deleteContraction(contractionId: Long) {
        state.value = state.value.copy(
            contractions = state.value.contractions.filterNot { it.id == contractionId }
        )
    }
}

private class FakeDeleteWaterBreakRepository(
    history: List<WaterBreakEvent>
) : WaterBreakRepository {
    private val state = MutableStateFlow(history)

    override fun observeActiveEvent(userId: String): Flow<WaterBreakEvent?> = flowOf(state.value.firstOrNull { it.isActive })

    override fun observeHistory(userId: String): MutableStateFlow<List<WaterBreakEvent>> = state

    override suspend fun createEvent(userId: String, happenedAt: Instant, color: WaterColor, notes: String): Long = 1L

    override suspend fun closeActiveEvent(userId: String, closedAt: Instant) = Unit

    override suspend fun deleteEvent(eventId: Long) {
        state.value = state.value.filterNot { it.id == eventId }
    }
}

