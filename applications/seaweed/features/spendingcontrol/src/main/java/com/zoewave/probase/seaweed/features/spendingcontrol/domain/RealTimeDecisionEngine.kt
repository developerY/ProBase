package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import javax.inject.Inject

class RealTimeDecisionEngine @Inject constructor(
    private val repository: EnvelopeRepository
) : DecisionEngine {

    override suspend fun evaluateTransaction(
        amountCents: Long,
        categoryId: String
    ): SpendingDecision {
        val envelope = repository.getEnvelopeForCategory(categoryId) ?: return SpendingDecision.Approved

        val potentialTotal = envelope.currentSpentCents + amountCents
        
        return when {
            potentialTotal > envelope.monthlyLimitCents -> {
                SpendingDecision.Declined(
                    reason = "${envelope.name} limit exceeded",
                    envelopeId = envelope.id
                )
            }
            potentialTotal > envelope.monthlyLimitCents * 0.9 -> {
                SpendingDecision.Warning(
                    message = "${envelope.name} is nearly full (90%)"
                )
            }
            else -> SpendingDecision.Approved
        }
    }
}
