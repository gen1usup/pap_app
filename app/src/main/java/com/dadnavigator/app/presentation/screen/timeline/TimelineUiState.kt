package com.dadnavigator.app.presentation.screen.timeline

import com.dadnavigator.app.domain.model.TimelineEvent
import com.dadnavigator.app.domain.model.TimelineType

enum class TimelineFilter {
    ALL,
    LABOR,
    POSTPARTUM
}

data class TimelineUiState(
    val filter: TimelineFilter = TimelineFilter.ALL,
    val events: List<TimelineEvent> = emptyList(),
    val expandedEventIds: Set<Long> = emptySet(),
    val showAddSheet: Boolean = false,
    val selectedType: TimelineType = TimelineType.LABOR,
    val draftTitle: String = "",
    val draftDescription: String = "",
    val infoRes: Int? = null,
    val errorRes: Int? = null
)
