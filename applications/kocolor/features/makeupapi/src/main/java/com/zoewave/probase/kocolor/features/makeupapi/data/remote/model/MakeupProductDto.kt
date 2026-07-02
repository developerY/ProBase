package com.zoewave.probase.kocolor.features.makeupapi.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MakeupProductDto(
    val id: Int,
    val brand: String? = null,
    val name: String,
    val price: String? = null,
    @SerialName("price_sign") val priceSign: String? = null,
    val currency: String? = null,
    @SerialName("product_type") val productType: String? = null,
    val category: String? = null,
    @SerialName("tag_list") val tagList: List<String> = emptyList()
)
