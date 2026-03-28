package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Event categories for unified timeline.
 */
enum class TimelineType {
    CONTRACTION,
    WATER_BREAK,
    LABOR,
    BIRTH,
    PREPARATION_NOTE,
    LABOR_NOTE,
    BABY_NOTE,
    FEEDING,
    DIAPER,
    SLEEP,
    NOTE
}

enum class TimelineEntryType {
    SYSTEM,
    USER_NOTE
}

fun TimelineType.defaultEntryType(): TimelineEntryType = when (this) {
    TimelineType.PREPARATION_NOTE,
    TimelineType.LABOR_NOTE,
    TimelineType.BABY_NOTE,
    TimelineType.NOTE -> TimelineEntryType.USER_NOTE
    else -> TimelineEntryType.SYSTEM
}

/**
 * Chronology event that unifies all critical records.
 */
data class TimelineEvent(
    val id: Long,
    val userId: String,
    val type: TimelineType,
    val timestamp: Instant,
    val title: String,
    val description: String,
    val stageAtCreation: AppStage = AppStage.PREPARING,
    val entryType: TimelineEntryType = type.defaultEntryType()
)
