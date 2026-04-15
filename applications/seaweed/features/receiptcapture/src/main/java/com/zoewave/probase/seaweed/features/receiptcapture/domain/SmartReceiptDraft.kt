package com.zoewave.probase.seaweed.features.receiptcapture.domain

import kotlinx.serialization.Serializable

@Serializable
data class SmartReceiptDraft(
    val merchant: String? = null,
    val total: Double? = null,
    val date: String? = null, // MM/DD/YYYY formatted
    val category: String? = null,
    val photoUri: String? = null
)
