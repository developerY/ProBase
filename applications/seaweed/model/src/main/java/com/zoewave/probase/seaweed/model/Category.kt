package com.zoewave.probase.seaweed.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val defaultType: SpendingType,
    val icon: String? = null
)
