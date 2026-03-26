package com.dadnavigator.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadnavigator.app.data.local.entity.ContractionEntity
import com.dadnavigator.app.data.local.entity.ContractionSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * DAO for contractions and sessions.
 */
@Dao
interface ContractionDao {

    @Query(
        """
        SELECT * FROM contraction_sessions
        WHERE userId = :userId AND endedAt IS NULL
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    suspend fun getActiveSession(userId: String): ContractionSessionEntity?

    @Query(
        """
        SELECT * FROM contraction_sessions
        WHERE userId = :userId AND endedAt IS NULL
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    fun observeActiveSession(userId: String): Flow<ContractionSessionEntity?>

    @Query(
        """
        SELECT * FROM contraction_sessions
        WHERE userId = :userId
        ORDER BY startedAt DESC
        """
    )
    fun observeSessions(userId: String): Flow<List<ContractionSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(entity: ContractionSessionEntity): Long

    @Query("UPDATE contraction_sessions SET endedAt = :endedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, endedAt: Instant)

    @Query(
        """
        SELECT * FROM contractions
        WHERE sessionId = :sessionId
        ORDER BY startedAt ASC
        """
    )
    fun observeContractions(sessionId: Long): Flow<List<ContractionEntity>>

    @Query(
        """
        SELECT * FROM contractions
        WHERE sessionId = :sessionId AND endedAt IS NULL
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    fun observeActiveContraction(sessionId: Long): Flow<ContractionEntity?>

    @Query(
        """
        SELECT * FROM contractions
        WHERE sessionId = :sessionId AND endedAt IS NULL
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    suspend fun getActiveContraction(sessionId: Long): ContractionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContraction(entity: ContractionEntity): Long

    @Query("UPDATE contractions SET endedAt = :endedAt WHERE id = :contractionId")
    suspend fun finishContraction(contractionId: Long, endedAt: Instant)

    @Query("UPDATE contractions SET endedAt = :endedAt WHERE sessionId = :sessionId AND endedAt IS NULL")
    suspend fun finishActiveContractionsInSession(sessionId: Long, endedAt: Instant)
}
