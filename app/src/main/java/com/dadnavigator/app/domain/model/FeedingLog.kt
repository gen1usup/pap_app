package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Feeding mode classification.
 */
enum class FeedingType {
    LEFT,
    RIGHT,
    BOTTLE
}

/**
 * Feeding log record.
 */
data class FeedingLog(
    val id: Long,
    val userId: String,
    val timestamp: Instant,
    val durationMinutes: Int,
    val type: FeedingType
)
