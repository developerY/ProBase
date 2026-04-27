package com.zoewave.probase.seaweed.features.spendingcontrol.domain

sealed interface SpendingDecision {
    /**
     * Within limit, approve instantly.
     */
    data object Approved : SpendingDecision

    /**
     * Near limit, approve but notify user.
     */
    data class Warning(val message: String) : SpendingDecision

    /**
     * Exceeds limit, trigger intervention flow.
     */
    data class Declined(val reason: String, val envelopeId: String) : SpendingDecision
}
