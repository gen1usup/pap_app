package com.dadnavigator.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relation for checklist with its items.
 */
data class ChecklistWithItemsRelation(
    @Embedded val checklist: ChecklistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "checklistId"
    )
    val items: List<ChecklistItemEntity>
)
