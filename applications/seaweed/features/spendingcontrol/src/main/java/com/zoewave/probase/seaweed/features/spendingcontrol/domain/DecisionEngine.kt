package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import com.zoewave.probase.seaweed.model.Transaction

interface DecisionEngine {
    /**
     * Evaluates a potential transaction against active envelopes.
     */
    suspend fun evaluateTransaction(
        amountCents: Long,
        categoryId: String
    ): SpendingDecision
}
