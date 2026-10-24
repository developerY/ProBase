package com.zoewave.probase.goswift.mobile.shots.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.goswift.data.ShotRepository
import com.zoewave.probase.goswift.model.CaffeineShot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShotsViewModel @Inject constructor(
    private val repository: ShotRepository
) : ViewModel() {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MMM dd")

    val uiState: StateFlow<ShotsUiState> = repository.getAllShots()
        .map { shots ->
            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.now(zoneId)
            val todayShots = shots.filter { shot ->
                Instant.ofEpochMilli(shot.timestamp).atZone(zoneId).toLocalDate() == today
            }

            val mappedItems = if (todayShots.isNotEmpty()) {
                todayShots.mapIndexed { index, shot ->
                    val doseNum = todayShots.size - index
                    DoseCardItem(
                        id = shot.id,
                        doseNumber = doseNum,
                        time = timeFormatter.format(Instant.ofEpochMilli(shot.timestamp)),
                        doseType = if (shot.mg <= 25) "Focus Sprint" else "Post-Lunch Rescue",
                        doseCategory = if (shot.mg <= 25) "MICRO-DROP" else "BOLUS DOSE",
                        amountMg = shot.mg,
                        title = "${shot.mg}mg Fuel Ingestion",
                        description = if (shot.mg <= 25) "Sublingual drops (Yuzu Citrus)" else "AeroPress Single Origin • Light Roast",
                        tags = if (shot.mg <= 25) listOf("💚 HRV +4%", "💧 +180ml H2O", "✓ Cleared In-System") else listOf("⚡ Glycemic Stable", "✓ On-Device Logged"),
                        accentColorHex = if (shot.mg <= 25) 0xFFFF6F61 else 0xFFFDBA74,
                        iconType = if (shot.mg <= 25) "bolt" else "coffee"
                    )
                }
            } else null

            val dateLabel = "Today, ${today.format(monthDayFormatter)}"

            val todayGroup = if (mappedItems != null) {
                DayDoseGroup(
                    dateLabel = dateLabel,
                    doseCount = mappedItems.size,
                    totalMg = mappedItems.sumOf { it.amountMg },
                    rangeStatus = "Optimal Range",
                    items = mappedItems
                )
            } else {
                DayDoseGroup(
                    dateLabel = dateLabel,
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
                )
            }

            val totalDoses = 10482 + shots.size

            ShotsUiState.Success(
                shots = shots,
                totalTelemetryDoses = totalDoses,
                todayGroup = todayGroup
            ) as ShotsUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ShotsUiState.Loading
        )

    fun onEvent(event: ShotsUiEvent) {
        when (event) {
            is ShotsUiEvent.DeleteShot -> {
                viewModelScope.launch {
                    repository.deleteShot(event.id)
                }
            }
            is ShotsUiEvent.LogShot -> {
                viewModelScope.launch {
                    repository.addShot(
                        CaffeineShot(
                            id = UUID.randomUUID().toString(),
                            mg = event.mg,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }
}

