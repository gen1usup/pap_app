package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * User profile model prepared for future multi-user sync scenario.
 */
data class AppUser(
    val id: String,
    val displayName: String,
    val createdAt: Instant
)
