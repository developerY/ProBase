package com.zoewave.probase.ashbike.wear.features.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.zoewave.ashbike.wear.settings.R


@Composable
fun WearSettingsPage(
    modifier: Modifier = Modifier,
    isHealthConnectEnabled: Boolean,
    onHealthConnectToggled: (Boolean) -> Unit,
    isMetricUnits: Boolean,
    onMetricUnitsToggled: (Boolean) -> Unit,
    isAutoPauseEnabled: Boolean,
    onAutoPauseToggled: (Boolean) -> Unit,
    onManageEBikeClick: () -> Unit,
    onNavigateToExperiments:  () -> Unit
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        // --- Header ---
        item {
            ListHeader {
                Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)//.title3)
            }
        }

        // --- Tracking Settings ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = isAutoPauseEnabled,
                onCheckedChange = onAutoPauseToggled,
                label = { Text(stringResource(R.string.auto_pause_label)) },
                secondaryLabel = { Text(stringResource(R.string.auto_pause_description), color = Color.Gray) },
                toggleControl = {
                    Switch(
                        checked = isAutoPauseEnabled,
                        onCheckedChange = null // Handled by the parent chip
                    )
                },
                appIcon = {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = stringResource(R.string.timer_icon_description))
                }
            )
        }

        // --- Display & Units ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = isMetricUnits,
                onCheckedChange = onMetricUnitsToggled,
                label = { Text(stringResource(R.string.use_metric_label)) },
                secondaryLabel = {
                    Text(
                        if (isMetricUnits) stringResource(R.string.metric_units_description) else stringResource(R.string.imperial_units_description),
                        color = Color.Gray
                    )
                },
                toggleControl = {
                    Switch(checked = isMetricUnits, onCheckedChange = null)
                },
                appIcon = {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = stringResource(R.string.units_icon_description))
                }
            )
        }

        // --- Health & Integrations ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = isHealthConnectEnabled,
                onCheckedChange = onHealthConnectToggled,
                label = { Text(stringResource(R.string.health_connect_label)) },
                secondaryLabel = { Text(stringResource(R.string.health_connect_description), color = Color.Gray) },
                toggleControl = {
                    Switch(checked = isHealthConnectEnabled, onCheckedChange = null)
                },
                appIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.health_icon_description),
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
                label = { Text(stringResource(R.string.manage_ebike_label)) },
                secondaryLabel = { Text(stringResource(R.string.manage_ebike_description), color = Color.Gray) },
                icon = {
                    Icon(imageVector = Icons.Default.ElectricBike, contentDescription = stringResource(R.string.ebike_icon_description))
                }
            )
        }

        // --- The Entry to the Experimental Hub ---
        // Put this as the absolute last item in the list
        item {
            CompactChip(
                onClick = onNavigateToExperiments, // Triggers Nav3 to push the new screen over the pager
                label = { Text(stringResource(R.string.ashbike_labs_label), color = Color.Gray) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = stringResource(R.string.experiments_icon_description),
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }



    }
}