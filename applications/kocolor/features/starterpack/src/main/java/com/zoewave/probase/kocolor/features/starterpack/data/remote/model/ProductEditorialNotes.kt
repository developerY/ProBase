package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductEditorialNotes(
    val id: String,
    @SerialName("editorial_title") val editorialTitle: String,
    @SerialName("usage_notes") val usageNotes: String,
    @SerialName("expert_tip") val expertTip: String,
    @SerialName("formulation_insight") val formulationInsight: String? = null
)
