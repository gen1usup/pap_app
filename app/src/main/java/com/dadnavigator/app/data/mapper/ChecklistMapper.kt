package com.dadnavigator.app.data.mapper

import com.dadnavigator.app.data.local.entity.ChecklistEntity
import com.dadnavigator.app.data.local.entity.ChecklistItemEntity
import com.dadnavigator.app.data.local.entity.ChecklistWithItemsRelation
import com.dadnavigator.app.domain.model.Checklist
import com.dadnavigator.app.domain.model.ChecklistItem
import com.dadnavigator.app.domain.model.ChecklistWithItems

/**
 * Maps checklist entities.
 */
fun ChecklistWithItemsRelation.toDomain(): ChecklistWithItems = ChecklistWithItems(
    checklist = checklist.toDomain(),
    items = items.map { it.toDomain() }
)

fun ChecklistEntity.toDomain(): Checklist = Checklist(
    id = id,
    userId = userId,
    name = name,
    isSystem = isSystem,
    createdAt = createdAt
)

fun Checklist.toEntity(): ChecklistEntity = ChecklistEntity(
    id = id,
    userId = userId,
    name = name,
    isSystem = isSystem,
    createdAt = createdAt
)

fun ChecklistItemEntity.toDomain(): ChecklistItem = ChecklistItem(
    id = id,
    checklistId = checklistId,
    userId = userId,
    text = text,
    isChecked = isChecked,
    createdAt = createdAt
)

fun ChecklistItem.toEntity(): ChecklistItemEntity = ChecklistItemEntity(
    id = id,
    checklistId = checklistId,
    userId = userId,
    text = text,
    isChecked = isChecked,
    createdAt = createdAt
)
