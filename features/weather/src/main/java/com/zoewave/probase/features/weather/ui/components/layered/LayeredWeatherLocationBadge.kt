package com.zoewave.probase.features.weather.ui.components.layered

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A combined UI component that displays a location name alongside a compact weather info icon.
 * This component handles its own loading ("Locating...") and error (grayed-out icon) states.
 */
@Composable
fun LayeredWeatherLocationBadge(
    uiState: LayeredWeatherUiState?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Location Text
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = (uiState?.locationName ?: "Locating...").uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }

        // Weather Icon or Fallback
        if (uiState != null) {
            LayeredWeatherInfoIcon(
                uiState = uiState,
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 16.dp, y = (-16).dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 16.dp, y = (-16).dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Weather unavailable",
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LayeredWeatherLocationBadgePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            // Success State
            LayeredWeatherLocationBadge(
                uiState = LayeredWeatherUiState(
                    locationName = "Manhattan",
                    temperature = 24.0,
                    uvIndex = 0.0,
                    conditions = listOf(LayeredWeatherCondition.CLOUDY)
                )
            )
            
            // Loading State
            LayeredWeatherLocationBadge(uiState = null)
        }
    }
}
