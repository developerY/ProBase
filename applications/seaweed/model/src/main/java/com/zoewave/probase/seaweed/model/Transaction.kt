package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val amountCents: Long,
    val categoryId: String,
    val description: String,
    val timestamp: Long,
    val receiptUri: String? = null,
    
    // Core behavior system
    val defaultType: SpendingType,
    val userOverrideType: SpendingType? = null,
    
    // Recurring detection / grouping
    val recurringId: String? = null,

    // Location data
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val effectiveType: SpendingType
        get() = userOverrideType ?: defaultType
}
