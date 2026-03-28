package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dadnavigator.app.data.local.entity.ChecklistEntity
import com.dadnavigator.app.data.local.entity.ChecklistItemEntity
import com.dadnavigator.app.data.local.entity.ChecklistWithItemsRelation
import kotlinx.coroutines.flow.Flow

/**
 * DAO for checklist storage.
 */
@Dao
interface ChecklistDao {

    @Transaction
    @Query(
        """
        SELECT * FROM checklists
        WHERE userId = :userId AND isDeleted = 0
        ORDER BY sortOrder ASC, createdAt ASC
        """
    )
    fun observeChecklists(userId: String): Flow<List<ChecklistWithItemsRelation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(entity: ChecklistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(entities: List<ChecklistItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(entity: ChecklistItemEntity)

    @Query(
        """
        UPDATE checklists
        SET title = :title
        WHERE id = :checklistId AND userId = :userId AND isSystem = 0 AND isDeleted = 0
        """
    )
    suspend fun renameChecklist(userId: String, checklistId: Long, title: String)

    @Query("UPDATE checklist_items SET isChecked = :checked WHERE id = :itemId AND userId = :userId")
    suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean)

    @Query("DELETE FROM checklist_items WHERE id = :itemId AND userId = :userId")
    suspend fun deleteItem(userId: String, itemId: Long)

    @Query("DELETE FROM checklist_items WHERE checklistId = :checklistId AND userId = :userId")
    suspend fun deleteItemsByChecklist(userId: String, checklistId: Long)

    @Query(
        """
        UPDATE checklists
        SET isDeleted = 1
        WHERE id = :checklistId AND userId = :userId
        """
    )
    suspend fun softDeleteChecklist(userId: String, checklistId: Long)

    @Transaction
    suspend fun deleteChecklistWithItems(userId: String, checklistId: Long) {
        deleteItemsByChecklist(userId, checklistId)
        softDeleteChecklist(userId, checklistId)
    }

    @Query("SELECT COUNT(*) FROM checklists WHERE userId = :userId AND isDeleted = 0")
    suspend fun countActiveChecklists(userId: String): Int

    @Query("SELECT COUNT(*) FROM checklists WHERE userId = :userId")
    suspend fun countAllChecklists(userId: String): Int

    @Query("DELETE FROM checklists")
    suspend fun clearChecklists()

    @Query("DELETE FROM checklist_items")
    suspend fun clearItems()
}
