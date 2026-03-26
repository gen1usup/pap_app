package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for feeding logs.
 */
@Entity(
    tableName = "feeding_logs",
    indices = [Index("userId"), Index("timestamp")]
)
data class FeedingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Instant,
    val durationMinutes: Int,
    val type: String
)

/**
 * Room entity for diaper logs.
 */
@Entity(
    tableName = "diaper_logs",
    indices = [Index("userId"), Index("timestamp")]
)
data class DiaperLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Instant,
    val type: String,
    val notes: String
)

/**
 * Room entity for sleep logs.
 */
@Entity(
    tableName = "sleep_logs",
    indices = [Index("userId"), Index("startTime")]
)
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val startTime: Instant,
    val endTime: Instant,
    val notes: String
)

/**
 * Room entity for notes.
 */
@Entity(
    tableName = "notes",
    indices = [Index("userId"), Index("timestamp")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Instant,
    val text: String,
    val category: String
)
