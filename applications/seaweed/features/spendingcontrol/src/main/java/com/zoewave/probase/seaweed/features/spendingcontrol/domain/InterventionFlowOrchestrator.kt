package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterventionFlowOrchestrator @Inject constructor(
    private val decisionEngine: DecisionEngine,
    private val classifier: TransactionClassifier,
    private val repository: EnvelopeRepository
) {
    private val _interventionState = MutableStateFlow<InterventionState?>(null)
    val interventionState: StateFlow<InterventionState?> = _interventionState.asStateFlow()

    /**
     * Intercepts a transaction attempt.
     */
    suspend fun interceptTransaction(
        merchantName: String,
        amountCents: Long
    ): TransactionStatus {
        val authContext = AuthContext(merchantName, amountCents)
        val classification = classifier.classify(authContext)
        
        val decision = decisionEngine.evaluateTransaction(amountCents, classification.categoryId)

        return when (decision) {
            SpendingDecision.Approved -> TransactionStatus.Approved
            is SpendingDecision.Warning -> {
                // Return approved but with a warning (can be handled by UI via notifications)
                TransactionStatus.ApprovedWithWarning(decision.message)
            }
            is SpendingDecision.Declined -> {
                val state = InterventionState(
                    merchantName = merchantName,
                    amountCents = amountCents,
                    categoryId = classification.categoryId,
                    envelopeId = decision.envelopeId,
                    reason = decision.reason
                )
                _interventionState.value = state
                TransactionStatus.Declined(decision.reason)
            }
        }
    }

    suspend fun resolveIntervention(action: InterventionAction) {
        val currentState = _interventionState.value ?: return

        when (action) {
            InterventionAction.Override -> {
                // Logic to mark as allowed for next retry
                _interventionState.value = null
            }
            is InterventionAction.MoveFunds -> {
                // Move funds from source to target envelope
                // For simplicity, just updating spent amounts
                repository.updateSpentAmount(action.sourceEnvelopeId, -currentState.amountCents)
                _interventionState.value = null
            }
            InterventionAction.Cancel -> {
                _interventionState.value = null
            }
        }
    }
}

data class InterventionState(
    val merchantName: String,
    val amountCents: Long,
    val categoryId: String,
    val envelopeId: String,
    val reason: String
)

sealed interface InterventionAction {
    data object Override : InterventionAction
    data class MoveFunds(val sourceEnvelopeId: String) : InterventionAction
    data object Cancel : InterventionAction
}

sealed interface TransactionStatus {
    data object Approved : TransactionStatus
    data class ApprovedWithWarning(val warning: String) : TransactionStatus
    data class Declined(val reason: String) : TransactionStatus
}
