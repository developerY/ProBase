package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class CreditCard(
    val id: String,
    val name: String,
    val bankName: String,
    val lastFourDigits: String,
    
    val creditLimitCents: Long,
    val interestRateApr: Double,
    
    val statementDay: Int, // 1 to 31
    val dueDay: Int,       // 1 to 31
    
    val isFrozen: Boolean = false,
    val colorHex: String? = null,
    
    // Future-proofing for AI guidance
    val cardPurposeContext: String? = null, // e.g. "Primary for travel"
    val dailyTransactionLimitCents: Long? = null,
    val maxSingleTransactionCents: Long? = null
)

@Serializable
data class CardReward(
    val cardId: String,
    val categoryId: String,
    val multiplier: Double, // e.g. 3.0 for 3x points
    val rewardType: String? = null // e.g. "Cashback", "Points"
)
