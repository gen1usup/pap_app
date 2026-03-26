package com.dadnavigator.app.domain.model

import java.time.Instant

/**
 * Key labor metadata entered by user.
 */
data class LaborSummary(
    val laborStartTime: Instant?,
    val birthTime: Instant?,
    val birthWeightGrams: Int?,
    val birthHeightCm: Int?
)
