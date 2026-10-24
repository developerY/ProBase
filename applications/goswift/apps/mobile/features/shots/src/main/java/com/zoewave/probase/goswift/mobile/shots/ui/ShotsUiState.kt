package com.zoewave.probase.goswift.mobile.shots.ui

import com.zoewave.probase.goswift.model.CaffeineShot

data class DoseCardItem(
    val id: String,
    val doseNumber: Int,
    val time: String,
    val doseType: String,
    val doseCategory: String,
    val amountMg: Int,
    val title: String,
    val description: String,
    val tags: List<String>,
    val accentColorHex: Long = 0xFFFF6F61,
    val iconType: String = "bolt"
)

data class DayDoseGroup(
    val dateLabel: String,
    val doseCount: Int,
    val totalMg: Int,
    val rangeStatus: String = "Optimal Range",
    val items: List<DoseCardItem>
)

sealed interface ShotsUiState {
    object Loading : ShotsUiState
    data class Success(
        val shots: List<CaffeineShot> = emptyList(),
        val totalTelemetryDoses: Int = 10482,
        val dailyVelocityMg: Int = 72,
        val avgPulses: Double = 3.4,
        val fidelityPercent: Double = 99.4,
        val activeMicrodoseNumber: Int = 68,
        val remainingInBloodstreamMg: Double = 28.4,
        val todayGroup: DayDoseGroup = DayDoseGroup(
            dateLabel = "Today, Oct 24",
            doseCount = 3,
            totalMg = 100,
            rangeStatus = "Optimal Range",
            items = listOf(
                DoseCardItem(
                    id = "68",
                    doseNumber = 68,
                    time = "04:02 PM",
                    doseType = "Focus Sprint",
                    doseCategory = "MICRO-DROP",
                    amountMg = 15,
                    title = "15mg Fuel Ingestion",
                    description = "Sublingual drops (Yuzu Citrus)",
                    tags = listOf("💚 HRV +4%", "💧 +180ml H2O", "✓ Cleared In-System"),
                    accentColorHex = 0xFFFF6F61,
                    iconType = "bolt"
                ),
                DoseCardItem(
                    id = "67",
                    doseNumber = 67,
                    time = "02:18 PM",
                    doseType = "Post-Lunch Rescue",
                    doseCategory = "BOLUS DOSE",
                    amountMg = 65,
                    title = "65mg Fuel Ingestion",
                    description = "AeroPress Single Origin • Light Roast",
                    tags = listOf("⚡ Glycemic Stable", "✓ On-Device Logged"),
                    accentColorHex = 0xFFFDBA74,
                    iconType = "coffee"
                ),
                DoseCardItem(
                    id = "66",
                    doseNumber = 66,
                    time = "10:56 AM",
                    doseType = "Morning Prime",
                    doseCategory = "MICRO-DROP",
                    amountMg = 20,
                    title = "20mg Fuel Ingestion",
                    description = "Precision Hydration Spritz • Electrolytes",
                    tags = listOf("⚡ Baseline Active", "🕒 On Schedule"),
                    accentColorHex = 0xFFFEF08A,
                    iconType = "sun"
                )
            )
        ),
        val yesterdayCount: Int = 4,
        val yesterdayTotalMg: Int = 175,
        val yesterdaySummaryText: String = "#65 - #62 (65mg, 20mg, 25mg, 65mg)...",
        val monthlyTotalDoses: Int = 142,
        val monthlyTotalMassGrams: Double = 2.84,
        val monthlyCurfewScorePercent: Int = 100,
        val monthlyAvgIntervalHours: Double = 3.1
    ) : ShotsUiState
}

sealed interface ShotsUiEvent {
    data class DeleteShot(val id: String) : ShotsUiEvent
    data class LogShot(val mg: Int) : ShotsUiEvent
}
