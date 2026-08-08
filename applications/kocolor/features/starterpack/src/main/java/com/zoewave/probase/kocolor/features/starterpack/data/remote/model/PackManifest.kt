package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackManifest(
    @SerialName("manifest_version") val manifestVersion: Int,
    @SerialName("generated_at") val generatedAt: String,
    @SerialName("compiler_version") val compilerVersion: String,
    @SerialName("key_id") val keyId: String,
    val packs: List<PackInfo>
)

@Serializable
data class PackInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: Int,
    val publisher: String,
    @SerialName("type") val packType: String,
    val endpoint: String,
    @SerialName("item_count") val itemCount: Int,
    @SerialName("compressed_size_bytes") val compressedSizeBytes: Long,
    @SerialName("uncompressed_size_bytes") val uncompressedSizeBytes: Long,
    val sha256: String,
    val signature: String,
    @SerialName("compression_algorithm") val compressionAlgorithm: String,
    @SerialName("hash_algorithm") val hashAlgorithm: String,
    @SerialName("hash_encoding") val hashEncoding: String,
    @SerialName("signature_algorithm") val signatureAlgorithm: String,
    @SerialName("signature_encoding") val signatureEncoding: String,
    @SerialName("package_format_version") val packageFormatVersion: Int,
    @SerialName("schema_version") val schemaVersion: Int,
    val encryption: String,
    @SerialName("hero_image_url") val heroImageUrl: String? = null,
    @SerialName("expires_at") val expiresAt: Long? = null,
    @SerialName("preview_items") val previewItems: List<PreviewItem> = emptyList()
)

@Serializable
data class PreviewItem(
    val name: String,
    val description: String
)
