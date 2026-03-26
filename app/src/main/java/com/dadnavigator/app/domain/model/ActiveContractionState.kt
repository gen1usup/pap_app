package com.dadnavigator.app.domain.model

/**
 * Aggregated active contraction session data for a single stream subscription.
 */
data class ActiveContractionState(
    val session: ContractionSession?,
    val contractions: List<Contraction>,
    val activeContraction: Contraction?
)
