package com.zoewave.probase.seaweed.features.spendingcontrol.domain

import com.zoewave.probase.seaweed.model.SpendingType

data class AuthContext(
    val merchantName: String,
    val amountCents: Long,
    val location: String? = null
)

data class ClassificationResult(
    val categoryId: String,
    val spendingType: SpendingType
)

interface TransactionClassifier {
    /**
     * Classifies a transaction based on the merchant and other context.
     */
    suspend fun classify(context: AuthContext): ClassificationResult
}
