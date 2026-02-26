package com.zoewave.probase.ashbike.wear.features.rides.ui.health

import java.time.DayOfWeek

// ==========================================
// 1. UI Data Model
// ==========================================
data class DailyHeartRate(
    val dayOfWeek: DayOfWeek,
    val avgHr: Int,
    val maxHr: Int
)