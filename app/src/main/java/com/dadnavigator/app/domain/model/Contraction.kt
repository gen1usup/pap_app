package com.dadnavigator.app.domain.model

import java.time.Duration
import java.time.Instant

/**
 * Individual contraction event inside a session.
 */
data class Contraction(
    val id: Long,
    val sessionId: Long,
    val userId: String,
    val startedAt: Instant,
    val endedAt: Instant?
) {
    val duration: Duration?
        get() = endedAt?.let { Duration.between(startedAt, it) }
}
