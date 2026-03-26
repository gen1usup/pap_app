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
    FEEDING,
    DIAPER,
    SLEEP,
    NOTE
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
    val description: String
)
