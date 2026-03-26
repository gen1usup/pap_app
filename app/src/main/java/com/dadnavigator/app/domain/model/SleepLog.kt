package com.dadnavigator.app.domain.model

import java.time.Duration
import java.time.Instant

/**
 * Baby sleep period record.
 */
data class SleepLog(
    val id: Long,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant,
    val notes: String
) {
    val duration: Duration = Duration.between(startTime, endTime)
}
