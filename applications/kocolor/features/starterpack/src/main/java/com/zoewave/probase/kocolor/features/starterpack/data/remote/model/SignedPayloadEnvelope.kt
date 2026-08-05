package com.zoewave.probase.kocolor.features.starterpack.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SignedPayloadEnvelope<T>(
    val data: T,
    val signature: String,
    @SerialName("package_version") val packageVersion: String,
    @SerialName("schema_version") val schemaVersion: Int
)
