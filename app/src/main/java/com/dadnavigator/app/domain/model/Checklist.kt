package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Checklist root entity.
 */
data class Checklist(
    val id: Long,
    val userId: String,
    val name: String,
    val isSystem: Boolean,
    val createdAt: Instant
)

/**
 * Checklist item with completion state.
 */
data class ChecklistItem(
    val id: Long,
    val checklistId: Long,
    val userId: String,
    val text: String,
    val isChecked: Boolean,
    val createdAt: Instant
)

/**
 * Checklist with resolved items.
 */
data class ChecklistWithItems(
    val checklist: Checklist,
    val items: List<ChecklistItem>
) {
    val completedCount: Int = items.count { it.isChecked }
    val totalCount: Int = items.size
}
