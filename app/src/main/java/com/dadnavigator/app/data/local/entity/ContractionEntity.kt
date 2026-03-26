package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for individual contraction event.
 */
@Entity(
    tableName = "contractions",
    foreignKeys = [
        ForeignKey(
            entity = ContractionSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("userId")]
)
data class ContractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val userId: String,
    val startedAt: Instant,
    val endedAt: Instant?
)
