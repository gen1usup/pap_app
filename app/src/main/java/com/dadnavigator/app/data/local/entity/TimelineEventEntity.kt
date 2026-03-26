package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for timeline record.
 */
@Entity(
    tableName = "timeline_events",
    indices = [Index("userId"), Index("timestamp")]
)
data class TimelineEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val type: String,
    val timestamp: Instant,
    val title: String,
    val description: String
)
