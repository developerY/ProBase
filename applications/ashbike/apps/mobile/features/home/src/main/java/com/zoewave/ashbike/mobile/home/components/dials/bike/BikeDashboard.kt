package com.zoewave.ashbike.mobile.home.components.dials.bike

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zoewave.ashbike.mobile.home.ui.HomeEvent
import com.zoewave.ashbike.mobile.home.ui.HomeUiState


@RequiresApi(Build.VERSION_CODES.BAKLAVA)
@Composable
fun BikeDashboard(
    modifier: Modifier = Modifier,
    uiState: HomeUiState.Success, // Assumes this now has .glassGear
    onHomeEvent: (HomeEvent) -> Unit,
) {

            // --- NEW: 3-Column Row for Battery, Motor, Gear ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Gap between boxes
            ) {
                // 1. Battery Stat
                _root_ide_package_.com.zoewave.ashbike.mobile.home.components.dials.bike.BikeStatCard(
                    icon = Icons.Default.ElectricBolt, // Or your Battery icon
                    label = "Battery",
                    // Logic: If not null, show "100%", otherwise show "--%"
                    value = uiState.formattedBattery,
                    modifier = Modifier.weight(1f) // Equal width
                )

                // 2. Motor Stat
                _root_ide_package_.com.zoewave.ashbike.mobile.home.components.dials.bike.BikeStatCard(
                    icon = Icons.Default.PedalBike, // Or your Motor icon
                    label = "Motor",
                    // Logic: takeIf { it > 0 } returns null if 0, triggering the "--" fallback
                    value = uiState.formattedMotor, modifier = Modifier.weight(1f)
                )

                // 3. Gear Stat (From Glass)
                _root_ide_package_.com.zoewave.ashbike.mobile.home.components.dials.bike.BikeStatCard(
                    icon = Icons.Default.Settings, // Gear Icon
                    label = "Gear",
                    value = uiState.formattedGear, // Connected to Glass Data
                    modifier = Modifier.weight(1f)
                )
            }
            // --------------------------------------------------

            // "Tap to Connect" Button (Existing)
    _root_ide_package_.com.zoewave.ashbike.mobile.home.components.dials.bike.BikeCard(
        uiState = uiState,
        onHomeEvent = onHomeEvent
    )

        }
