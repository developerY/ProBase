package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class BudgetTarget(
    val categoryName: String,
    val limitAmountCents: Long
)
