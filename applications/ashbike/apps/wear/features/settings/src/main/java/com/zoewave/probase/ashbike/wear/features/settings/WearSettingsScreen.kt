package com.zoewave.probase.ashbike.wear.features.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.zoewave.ashbike.wear.settings.R

@Composable
fun WearSettingsPage(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateToAbout: () -> Unit,
    onManageEBikeClick: () -> Unit,
    onNavigateToExperiments: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        // --- Header ---
        item {
            ListHeader {
                Text(
                    text = stringResource(R.string.applications_ashbike_apps_wear_features_settings_settings_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // --- V1 Active Settings ---
        item {
            Chip(
                label = { Text("About AshBike") },
                secondaryLabel = { Text("Version 1.0.0") },
                onClick = onNavigateToAbout, // Wired up the navigation!
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "About icon"
                    )
                },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ==========================================
        // --- FUTURE WORK (Stashed for V1.1) ---
        // ==========================================
        /*
        // --- Tracking Settings ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = uiState.isAutoPauseEnabled,
                onCheckedChange = { onEvent(SettingsEvent.ToggleAutoPause(it)) },
                label = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_auto_pause_label)) },
                secondaryLabel = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_auto_pause_description), color = Color.Gray) },
                toggleControl = {
                    Switch(checked = uiState.isAutoPauseEnabled, onCheckedChange = null)
                },
                appIcon = {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = stringResource(R.string.applications_ashbike_apps_wear_features_settings_timer_icon_description))
                }
            )
        }

        // --- Display & Units ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = uiState.isMetricUnits,
                onCheckedChange = { onEvent(SettingsEvent.ToggleMetricUnits(it)) },
                label = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_use_metric_label)) },
                secondaryLabel = {
                    Text(
                        if (uiState.isMetricUnits) stringResource(R.string.applications_ashbike_apps_wear_features_settings_metric_units_description)
                        else stringResource(R.string.applications_ashbike_apps_wear_features_settings_imperial_units_description),
                        color = Color.Gray
                    )
                },
                toggleControl = {
                    Switch(checked = uiState.isMetricUnits, onCheckedChange = null)
                },
                appIcon = {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = stringResource(R.string.applications_ashbike_apps_wear_features_settings_units_icon_description))
                }
            )
        }

        // --- Health & Integrations ---
        item {
            ToggleChip(
                modifier = Modifier.fillMaxWidth(),
                checked = uiState.isHealthConnectEnabled,
                onCheckedChange = { onEvent(SettingsEvent.ToggleHealthConnect(it)) },
                label = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_health_connect_label)) },
                secondaryLabel = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_health_connect_description), color = Color.Gray) },
                toggleControl = {
                    Switch(checked = uiState.isHealthConnectEnabled, onCheckedChange = null)
                },
                appIcon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.applications_ashbike_apps_wear_features_settings_health_icon_description),
                        tint = if (uiState.isHealthConnectEnabled) Color.Red else Color.Gray
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
                label = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_manage_ebike_label)) },
                secondaryLabel = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_manage_ebike_description), color = Color.Gray) },
                icon = {
                    Icon(imageVector = Icons.Default.ElectricBike, contentDescription = stringResource(R.string.applications_ashbike_apps_wear_features_settings_ebike_icon_description))
                }
            )
        }

        // --- The Entry to the Experimental Hub ---
        item {
            CompactChip(
                onClick = onNavigateToExperiments,
                label = { Text(stringResource(R.string.applications_ashbike_apps_wear_features_settings_ashbike_labs_label), color = Color.Gray) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = stringResource(R.string.applications_ashbike_apps_wear_features_settings_experiments_icon_description),
                        tint = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        */
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Small Watch"
)
@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xFF000000,
    showBackground = true,
    name = "Large Watch"
)
@Composable
fun WearSettingsPagePreview() {
    MaterialTheme {
        WearSettingsPage(
            uiState = SettingsUiState(), // Pass the default dummy state
            onEvent = {},
            onNavigateToAbout = {},
            onManageEBikeClick = {},
            onNavigateToExperiments = {}
        )
    }
}