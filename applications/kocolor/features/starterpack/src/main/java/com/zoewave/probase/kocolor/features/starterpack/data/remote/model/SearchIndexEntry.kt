package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchIndexEntry(
    val id: String,
    val term: String,
    val brand: String,
    val packId: String
)
