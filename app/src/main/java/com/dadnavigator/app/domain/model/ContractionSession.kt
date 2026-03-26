package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Contraction session groups contractions from one labor observation period.
 */
data class ContractionSession(
    val id: Long,
    val userId: String,
    val startedAt: Instant,
    val endedAt: Instant?
) {
    val isActive: Boolean = endedAt == null
}
