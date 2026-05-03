package com.zoewave.probase.gotmind.analytics

interface AnalyticsHelper {
    fun logEvent(event: AnalyticsEvent)
    suspend fun getFirebaseId(): String
}

data class AnalyticsEvent(
    val type: String,
    val extras: List<AnalyticsParam> = emptyList()
)

data class AnalyticsParam(
    val key: String,
    val value: String
)
