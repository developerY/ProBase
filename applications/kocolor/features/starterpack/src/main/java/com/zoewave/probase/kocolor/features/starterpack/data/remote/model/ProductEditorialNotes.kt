package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductEditorialNotes(
    @SerialName("product_id") val id: String,
    @SerialName("card_title") val editorialTitle: String,
    val description: String? = null,
    val summary: String? = null,
    @SerialName("dynamic_attributes") val attributes: List<EditorialAttribute> = emptyList()
)

@Serializable
data class EditorialAttribute(
    val label: String,
    val body: String
)
