package com.dadnavigator.app.domain.service

import com.dadnavigator.app.domain.model.ActiveContractionState
import com.dadnavigator.app.domain.model.Contraction
import com.dadnavigator.app.domain.model.ContractionStats
import com.dadnavigator.app.domain.usecase.contraction.CalculateContractionStatsUseCase
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Builds one reusable real-time snapshot of the current contractions state.
 */
class LiveContractionSnapshotBuilder @Inject constructor(
    private val calculateContractionStatsUseCase: CalculateContractionStatsUseCase
) {

    fun build(
        activeState: ActiveContractionState,
        now: Instant
    ): LiveContractionSnapshot {
        val stats = calculateContractionStatsUseCase(activeState.contractions)
        val sessionDuration = activeState.session?.let { Duration.between(it.startedAt, now) }
            ?: Duration.ZERO
        val currentContractionDuration = activeState.activeContraction?.let {
            Duration.between(it.startedAt, now)
        } ?: Duration.ZERO

        return LiveContractionSnapshot(
            isSessionActive = activeState.session?.isActive == true,
            sessionId = activeState.session?.id,
            activeContractionId = activeState.activeContraction?.id,
            sessionDuration = sessionDuration,
            currentContractionDuration = currentContractionDuration,
            currentInterval = currentInterval(activeState, now),
            stats = stats,
            contractions = activeState.contractions
        )
    }

    private fun currentInterval(
        activeState: ActiveContractionState,
        now: Instant
    ): Duration? {
        val sortedContractions = activeState.contractions.sortedBy { it.startedAt }
        val activeContraction = activeState.activeContraction

        return when {
            activeContraction != null -> {
                sortedContractions
                    .filterNot { it.id == activeContraction.id }
                    .lastOrNull()
                    ?.let { previous -> Duration.between(previous.startedAt, activeContraction.startedAt) }
            }

            sortedContractions.isNotEmpty() -> Duration.between(sortedContractions.last().startedAt, now)
            else -> null
        }
    }
}

data class LiveContractionSnapshot(
    val isSessionActive: Boolean,
    val sessionId: Long?,
    val activeContractionId: Long?,
    val sessionDuration: Duration,
    val currentContractionDuration: Duration,
    val currentInterval: Duration?,
    val stats: ContractionStats,
    val contractions: List<Contraction>
)
