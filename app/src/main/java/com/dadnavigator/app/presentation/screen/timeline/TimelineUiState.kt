package com.dadnavigator.app.presentation.screen.timeline

import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType

enum class TimelineFilter {
    ALL,
    PREPARING,
    LABOR,
    BABY_BORN,
    NOTES
}

data class TimelineUiState(
    val filter: TimelineFilter = TimelineFilter.ALL,
    val events: List<TimelineEvent> = emptyList(),
    val expandedEventIds: Set<Long> = emptySet(),
    val showAddSheet: Boolean = false,
    val selectedType: TimelineType = TimelineType.NOTE,
    val draftTitle: String = "",
    val draftDescription: String = "",
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
