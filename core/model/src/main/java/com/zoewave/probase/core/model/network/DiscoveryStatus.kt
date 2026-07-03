package com.zoewave.probase.core.model.network

enum class ServiceStatus {
    IDLE, ACCESSING, SUCCESS, FAILED, UNSUPPORTED
}

data class ServiceHealth(
    val status: ServiceStatus = ServiceStatus.IDLE,
    val note: String? = null
)

data class DiscoveryStatus(
    val localAi: ServiceHealth = ServiceHealth(),
    val obf: ServiceHealth = ServiceHealth(),
    val fda: ServiceHealth = ServiceHealth(),
    val chemDb: ServiceHealth = ServiceHealth(),
    val colorApi: ServiceHealth = ServiceHealth(),
    val gemini: ServiceHealth = ServiceHealth(),
    val makeupApi: ServiceHealth = ServiceHealth()
)
