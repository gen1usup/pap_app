package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Diaper change type.
 */
enum class DiaperType {
    WET,
    DIRTY,
    MIXED
}

/**
 * Diaper change log record.
 */
data class DiaperLog(
    val id: Long,
    val userId: String,
    val timestamp: Instant,
    val type: DiaperType,
    val notes: String
)
