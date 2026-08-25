package com.zoewave.probase.features.ai.firebase.models

import kotlinx.serialization.Serializable

@Serializable
data class StyleTelemetry(
    val appearance: Appearance,
    val vaultManifest: String,
    val weatherContext: String,
    val circadianContext: String
)

@Serializable
data class Appearance(
    val temperature: String,
    val depth: String,
    val contrast: String
)
