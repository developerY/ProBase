package com.zoewave.probase.kocolor.db.entity

data class Provenance(
    val packId: String,
    val packVersion: String,
    val publisher: String,
    val installedAtTimestamp: Long,
    val isSignatureVerified: Boolean
)
