package com.zoewave.ashbike.mobile.home.components.dials.bike

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.ashbike.mobile.glass.ui.components.controls.LaunchGlassButton
import com.zoewave.ashbike.mobile.home.ui.HomeEvent
import com.zoewave.ashbike.mobile.home.ui.HomeUiState


@RequiresApi(Build.VERSION_CODES.S) // Adjusted to S (Android 12) or use your BAKLAVA import
@Composable
fun BikeCard(
    uiState: HomeUiState.Success, // We pass the WHOLE success state
    onHomeEvent: (HomeEvent) -> Unit
) {
    // 1. EXTRACT DATA (Cleanly)
    val isConnected = uiState.bikeData.isBikeConnected
    val batteryLevel = uiState.bikeData.batteryLevel

    // 2. COMPUTE VISUALS
    val containerColor = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isConnected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    // 3. COMPUTE TEXT (Or add this to HomeUiState as .statusTextVal)
    val statusText = if (isConnected) {
        "Battery: ${batteryLevel ?: 0}%"
    } else {
        "No Bike Connected"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium
            )

            // --- BATTERY INDICATOR ---
            // Only show the segmented bar if connected and we have data
            if (isConnected && batteryLevel != null) {
                Spacer(Modifier.height(8.dp))
                SegmentedBatteryIndicator(batteryLevel = batteryLevel)
            }

            Spacer(Modifier.height(16.dp))

            // --- GLASS CONTROL ---
            LaunchGlassButton(
                buttonState = uiState.glassButtonState,
                onButtonClick = { onHomeEvent(HomeEvent.ToggleGlassProjection) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}