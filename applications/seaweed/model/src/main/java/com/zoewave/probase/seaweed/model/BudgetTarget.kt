package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class BudgetTarget(
    val categoryId: String,
    val limitAmountCents: Long
)
