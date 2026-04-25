package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryOverview(
    val id: String,
    val name: String,
    val totalAmountCents: Long,
    val transactionCount: Int,
    val limitAmountCents: Long? = null,
    val remainingAmountCents: Long? = null,
    val progressPercentage: Float = 0f
)
