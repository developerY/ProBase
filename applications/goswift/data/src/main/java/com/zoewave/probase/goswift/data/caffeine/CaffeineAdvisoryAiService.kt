package com.zoewave.probase.goswift.data.caffeine

data class SanitizedCaffeineTrendPayload(
    val dailyConsumedMg: Double,
    val configuredDailyLimitMg: Double,
    val currentEstimatedRemainingMg: Double,
    val hoursUntilSleepThreshold: Double,
    val intakeCountLast7Days: Int,
    val averageDailyMgLast7Days: Double
)

sealed interface AdvisorySummaryResult {
    data class Available(val summaryText: String, val isOnDevice: Boolean) : AdvisorySummaryResult
    data class FallbackDeterministic(val summaryText: String, val reason: String) : AdvisorySummaryResult
    data object DisabledByUser : AdvisorySummaryResult
}

interface CaffeineAdvisoryAiService {
    suspend fun generateTrendAdvisory(
        payload: SanitizedCaffeineTrendPayload,
        isAiEnabled: Boolean,
        isCloudAiConsentGranted: Boolean
    ): AdvisorySummaryResult
}

class DeterministicAdvisoryFallback : CaffeineAdvisoryAiService {
    override suspend fun generateTrendAdvisory(
        payload: SanitizedCaffeineTrendPayload,
        isAiEnabled: Boolean,
        isCloudAiConsentGranted: Boolean
    ): AdvisorySummaryResult {
        if (!isAiEnabled) return AdvisorySummaryResult.DisabledByUser

        val summary = buildString {
            append("You have consumed ${payload.dailyConsumedMg.toInt()}mg today. ")
            if (payload.dailyConsumedMg > payload.configuredDailyLimitMg) {
                append("You are over your daily limit. ")
            } else {
                append("You are within your daily limit. ")
            }
            if (payload.hoursUntilSleepThreshold > 0) {
                append("Clearance to sleep threshold in approx ${"%.1f".format(payload.hoursUntilSleepThreshold)} hours.")
            } else {
                append("You are below the sleep threshold.")
            }
        }

        return AdvisorySummaryResult.FallbackDeterministic(summary, "AI unavailable or disabled")
    }
}
