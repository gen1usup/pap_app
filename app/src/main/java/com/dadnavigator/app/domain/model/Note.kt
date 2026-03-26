package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Generic note record.
 */
data class Note(
    val id: Long,
    val userId: String,
    val timestamp: Instant,
    val text: String,
    val category: String
)
