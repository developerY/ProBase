package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchIndexEntry(
    val id: String,
    val term: String,
    val brand: String,
    @SerialName("pack_id") val packId: String
)
