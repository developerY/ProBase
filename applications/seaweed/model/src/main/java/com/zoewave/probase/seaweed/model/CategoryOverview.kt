package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOverview(
    val name: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val progressPercentage: Float = 0f // Optional: if we want to show budget progress later
)
