package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dadnavigator.app.data.local.entity.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for quick emergency contacts.
 */
@Dao
interface EmergencyContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY sortOrder ASC, id ASC")
    fun observeContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContacts(contacts: List<EmergencyContactEntity>)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun countContacts(): Int

    @Query("DELETE FROM emergency_contacts")
    suspend fun clearContacts()

    @Transaction
    suspend fun replaceContacts(contacts: List<EmergencyContactEntity>) {
        clearContacts()
        upsertContacts(contacts)
    }
}
