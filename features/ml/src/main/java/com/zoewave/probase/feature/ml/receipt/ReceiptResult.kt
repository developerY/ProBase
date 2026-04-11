package com.zoewave.probase.feature.ml.receipt

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptResult(
    val totalAmount: Double = 0.0,
    val date: String = "",
    val merchant: String? = null,
    val category: String? = null
)
