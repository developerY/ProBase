package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
data class Provenance(
    val packId: String,
    val packVersion: String,
    val publisher: String,
    val installedAtTimestamp: Long,
    val isSignatureVerified: Boolean
)
