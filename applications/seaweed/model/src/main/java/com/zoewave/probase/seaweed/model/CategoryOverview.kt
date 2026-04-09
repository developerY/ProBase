package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOverview(
    val name: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val limitAmount: Double? = null,
    val remainingAmount: Double? = null,
    val progressPercentage: Float = 0f
)
