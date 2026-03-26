package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for water break event.
 */
@Entity(
    tableName = "water_break_events",
    indices = [Index("userId")]
)
data class WaterBreakEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val happenedAt: Instant,
    val color: String,
    val notes: String,
    val closedAt: Instant?
)
