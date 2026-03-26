package com.dadnavigator.app.domain.model

import java.time.Duration
import java.time.Instant

/**
 * Possible amniotic fluid colors.
 */
enum class WaterColor {
    CLEAR,
    PINK,
    GREEN,
    BROWN
}

/**
 * Water break event tracked by father.
 */
data class WaterBreakEvent(
    val id: Long,
    val userId: String,
    val happenedAt: Instant,
    val color: WaterColor,
    val notes: String,
    val closedAt: Instant?
) {
    val isActive: Boolean = closedAt == null

    fun elapsed(now: Instant): Duration = Duration.between(happenedAt, now)
}
