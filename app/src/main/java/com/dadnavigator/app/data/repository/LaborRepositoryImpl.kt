package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.LaborDao
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.data.mapper.toEntity
import com.dadnavigator.app.domain.model.LaborSummary
import com.dadnavigator.app.domain.repository.LaborRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed implementation for labor summary.
 */
class LaborRepositoryImpl @Inject constructor(
    private val laborDao: LaborDao
) : LaborRepository {

    override fun observeLaborSummary(userId: String): Flow<LaborSummary> {
        return laborDao.observeLaborSummary(userId).map { entity ->
            entity?.toDomain() ?: LaborSummary(
                laborStartTime = null,
                birthTime = null,
                birthWeightGrams = null,
                birthHeightCm = null
            )
        }
    }

    override suspend fun saveLaborSummary(userId: String, summary: LaborSummary) {
        laborDao.upsertLaborSummary(summary.toEntity(userId))
    }
}
