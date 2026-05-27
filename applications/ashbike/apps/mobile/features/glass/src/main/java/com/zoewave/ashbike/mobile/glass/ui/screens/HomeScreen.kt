package com.zoewave.ashbike.mobile.glass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface
import com.zoewave.ashbike.mobile.glass.R
import com.zoewave.ashbike.mobile.glass.newui.sections.HeaderBar
import com.zoewave.ashbike.mobile.glass.ui.GlassUiEvent
import com.zoewave.ashbike.mobile.glass.ui.GlassUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: GlassUiState,
    areVisualsOn: Boolean,
    onEvent: (GlassUiEvent) -> Unit
) {
    Box(
        modifier = modifier
            .background(if (areVisualsOn) Color.Black else Color.Transparent)
            .surface()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. HEADER
            HeaderBar(
                isConnected = uiState.isBikeConnected,
                gear = uiState.currentGear,
                batteryZone = uiState.batteryZone,
                batteryText = uiState.formattedBattery,
                onGearUp = { onEvent(GlassUiEvent.GearUp) },
                onGearDown = { onEvent(GlassUiEvent.GearDown) }
            )

            Spacer(Modifier.weight(1f))

            // 2. MAIN BRANDING / WELCOME
            Text(
                text = "ASHBIKE",
                style = GlimmerTheme.typography.titleLarge,
                color = GlimmerTheme.colors.primary
            )
            Text(
                text = "Ride Ready",
                style = GlimmerTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.weight(1f))

            // 3. ACTION BUTTONS
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onEvent(GlassUiEvent.ChangeScreen(com.zoewave.ashbike.mobile.glass.ui.ScreenState.BIKE)) }) {
                    Text("START RIDE")
                }
                Button(onClick = { onEvent(GlassUiEvent.CloseApp) }) {
                    Text(stringResource(R.string.applications_ashbike_apps_mobile_features_glass_exit))
                }
            }
        }
    }
}
