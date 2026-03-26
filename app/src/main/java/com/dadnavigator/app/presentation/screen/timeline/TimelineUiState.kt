package com.dadnavigator.app.presentation.screen.timeline

import com.dadnavigator.app.domain.model.TimelineEvent

/**
 * Filter options for timeline screen.
 */
enum class TimelineFilter {
    ALL,
    LABOR,
    POSTPARTUM
}

/**
 * UI state for timeline screen.
 */
data class TimelineUiState(
    val filter: TimelineFilter = TimelineFilter.ALL,
    val events: List<TimelineEvent> = emptyList()
)
