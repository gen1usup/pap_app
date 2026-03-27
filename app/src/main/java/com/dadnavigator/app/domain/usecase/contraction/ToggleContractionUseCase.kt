package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.repository.ContractionRepository
import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Starts the next contraction or finishes the currently active one.
 *
 * The use case keeps the timer flow reusable from multiple entry points,
 * including the dedicated contraction screen and the stage-aware Events hub.
 */
class ToggleContractionUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository,
    private val startContractionSessionUseCase: StartContractionSessionUseCase,
    private val startContractionUseCase: StartContractionUseCase,
    private val finishContractionUseCase: FinishContractionUseCase,
    private val addTimelineEventUseCase: AddTimelineEventUseCase
) {
    suspend operator fun invoke(
        userId: String,
        sessionId: Long?,
        activeContractionId: Long?
    ): ToggleContractionResult {
        val resolvedSessionId = sessionId ?: startContractionSessionUseCase(userId)

        return if (activeContractionId == null) {
            startContractionUseCase(sessionId = resolvedSessionId, userId = userId)
            ToggleContractionResult.Started
        } else {
            val activeState = contractionRepository.observeActiveState(userId).first()
            val activeContraction = activeState.activeContraction
                ?: activeState.contractions.firstOrNull { it.id == activeContractionId }
            val endedAt = Instant.now()
            finishContractionUseCase(activeContractionId, endedAt)
            addTimelineEventUseCase(
                userId = userId,
                timestamp = endedAt,
                title = "",
                description = buildContractionDescription(
                    activeContractionStartedAt = activeContraction?.startedAt,
                    endedAt = endedAt,
                    previousContractionStartedAt = activeState.contractions
                        .filter { it.id != activeContractionId && it.endedAt != null }
                        .maxByOrNull { it.startedAt }
                        ?.startedAt
                ),
                type = TimelineType.CONTRACTION
            )
            ToggleContractionResult.Stopped
        }
    }
}

enum class ToggleContractionResult {
    Started,
    Stopped
}

private fun buildContractionDescription(
    activeContractionStartedAt: Instant?,
    endedAt: Instant,
    previousContractionStartedAt: Instant?
): String {
    if (activeContractionStartedAt == null) return ""

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        .withZone(ZoneId.systemDefault())
    val duration = Duration.between(activeContractionStartedAt, endedAt)
    val interval = previousContractionStartedAt?.let {
        Duration.between(it, activeContractionStartedAt)
    }

    return buildString {
        append("Начало: ")
        append(formatter.format(activeContractionStartedAt))
        append('\n')
        append("Конец: ")
        append(formatter.format(endedAt))
        append('\n')
        append("Длительность: ")
        append(duration.toCompactString())
        if (interval != null) {
            append('\n')
            append("Интервал: ")
            append(interval.toCompactString())
        }
    }
}

private fun Duration.toCompactString(): String {
    val totalSeconds = toSeconds()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
