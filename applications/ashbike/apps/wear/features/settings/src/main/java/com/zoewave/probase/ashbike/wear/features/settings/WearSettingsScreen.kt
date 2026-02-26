package com.zoewave.probase.ashbike.wear.features.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text


@Composable
fun WearSettingsPage(
    isHealthConnectEnabled: Boolean,
    onHealthConnectToggled: (Boolean) -> Unit,
    isMetricUnits: Boolean,
    onMetricUnitsToggled: (Boolean) -> Unit,
    isAutoPauseEnabled: Boolean,
    onAutoPauseToggled: (Boolean) -> Unit,
    onManageEBikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        // --- Header ---
        item {
            ListHeader {
                Text(text = "Settings", style = MaterialTheme.typography.titleMedium)//.title3)
            }
        }

        // --- Tracking Settings ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = isAutoPauseEnabled,
                onCheckedChange = onAutoPauseToggled,
                label = { Text("Auto-Pause") },
                secondaryLabel = { Text("Stop timer at 0 km/h", color = Color.Gray) },
                toggleControl = {
                    Switch(
                        checked = isAutoPauseEnabled,
                        onCheckedChange = null // Handled by the parent chip
                    )
                },
                appIcon = {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer")
                }
            )
        }

        // --- Display & Units ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = isMetricUnits,
                onCheckedChange = onMetricUnitsToggled,
                label = { Text("Use Metric") },
                secondaryLabel = { Text(if (isMetricUnits) "km, km/h" else "mi, mph", color = Color.Gray) },
                toggleControl = {
                    Switch(checked = isMetricUnits, onCheckedChange = null)
                },
                appIcon = {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Units")
                }
            )
        }

        // --- Health & Integrations ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = isHealthConnectEnabled,
                onCheckedChange = onHealthConnectToggled,
                label = { Text("Health Connect") },
                secondaryLabel = { Text("Sync HR & Calories", color = Color.Gray) },
                toggleControl = {
                    Switch(checked = isHealthConnectEnabled, onCheckedChange = null)
                },
                appIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Health",
                        tint = if (isHealthConnectEnabled) Color.Red else Color.Gray
                    )
                }
            )
        }

        // --- Hardware / E-Bike ---
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onManageEBikeClick,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text("Manage E-Bike") },
                secondaryLabel = { Text("Pair & Battery", color = Color.Gray) },
                icon = {
                    Icon(imageVector = Icons.Default.ElectricBike, contentDescription = "E-Bike")
                }
            )
        }
    }
}