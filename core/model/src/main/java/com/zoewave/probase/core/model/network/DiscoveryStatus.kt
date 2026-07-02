package com.zoewave.probase.core.model.network

enum class ServiceStatus {
    IDLE, ACCESSING, SUCCESS, FAILED
}

data class DiscoveryStatus(
    val obf: ServiceStatus = ServiceStatus.IDLE,
    val fda: ServiceStatus = ServiceStatus.IDLE,
    val chemDb: ServiceStatus = ServiceStatus.IDLE,
    val colorApi: ServiceStatus = ServiceStatus.IDLE,
    val gemini: ServiceStatus = ServiceStatus.IDLE,
    val makeupApi: ServiceStatus = ServiceStatus.IDLE
)
