package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long,
    val receiptUri: String? = null,
    val importance: SpendingImportance = SpendingImportance.REQUIRED
)
