package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.model.TimelineType
import com.dadnavigator.app.domain.usecase.timeline.AddTimelineEventUseCase
import java.time.Instant
import javax.inject.Inject

/**
 * Starts the next contraction or finishes the currently active one.
 *
 * The use case keeps the timer flow reusable from multiple entry points,
 * including the dedicated contraction screen and the stage-aware Events hub.
 */
class ToggleContractionUseCase @Inject constructor(
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
            finishContractionUseCase(activeContractionId)
            addTimelineEventUseCase(
                userId = userId,
                timestamp = Instant.now(),
                title = "",
                description = "",
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
