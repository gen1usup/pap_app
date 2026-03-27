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
        WHERE userId = :userId
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

    @Query("UPDATE checklist_items SET isChecked = :checked WHERE id = :itemId AND userId = :userId")
    suspend fun setItemChecked(userId: String, itemId: Long, checked: Boolean)

    @Query("SELECT COUNT(*) FROM checklists WHERE userId = :userId")
    suspend fun countChecklists(userId: String): Int

    @Query("DELETE FROM checklists")
    suspend fun clearChecklists()

    @Query("DELETE FROM checklist_items")
    suspend fun clearItems()
}
