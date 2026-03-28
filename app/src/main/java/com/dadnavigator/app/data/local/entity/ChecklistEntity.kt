package com.dadnavigator.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for checklist container.
 */
@Entity(
    tableName = "checklists",
    indices = [Index("userId")]
)
data class ChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val stage: String,
    val category: String,
    val isSystem: Boolean,
    val sortOrder: Int,
    val createdAt: Instant,
    val isDeleted: Boolean = false
)

/**
 * Room entity for checklist item.
 */
@Entity(
    tableName = "checklist_items",
    indices = [Index("checklistId"), Index("userId")]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val checklistId: Long,
    val userId: String,
    val text: String,
    val note: String? = null,
    val quantity: String? = null,
    val priority: Int? = null,
    val metadataJson: String? = null,
    val isChecked: Boolean,
    val createdAt: Instant
)
