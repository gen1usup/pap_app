package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for contraction session.
 */
@Entity(tableName = "contraction_sessions")
data class ContractionSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val startedAt: Instant,
    val endedAt: Instant?
)
