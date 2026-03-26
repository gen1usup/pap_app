package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.LaborSummary
import kotlinx.coroutines.flow.Flow

/**
 * Contract for storing key labor metadata.
 */
interface LaborRepository {
    fun observeLaborSummary(userId: String): Flow<LaborSummary>

    suspend fun saveLaborSummary(userId: String, summary: LaborSummary)
}
