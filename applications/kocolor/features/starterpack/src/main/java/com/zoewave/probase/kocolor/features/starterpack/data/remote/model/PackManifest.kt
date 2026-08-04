package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PackManifest(
    val packs: List<PackInfo>
)

@Serializable
data class PackInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: Int,
    val type: String, // STARTER_PACK, SAMPLE_PACK, etc.
    val endpoint: String, // e.g. "starter-pack.json"
    val item_count: Int
)
