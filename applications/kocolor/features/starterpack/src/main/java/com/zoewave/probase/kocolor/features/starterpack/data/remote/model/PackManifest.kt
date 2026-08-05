package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
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
    @SerialName("item_count") val itemCount: Int,
    @SerialName("size_bytes") val sizeBytes: Long? = null,
    val hash: String? = null,
    @SerialName("sha256") val sha256: String? = null,
    @SerialName("hero_image_url") val heroImageUrl: String? = null,
    @SerialName("expires_at") val expiresAt: Long? = null
)
