package com.zoewave.probase.ashbike.wear.features.rides.ui.dash

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
fun FeatureHubScreen(
    onNavigateToWeather: () -> Unit,
    onNavigateToElevation: () -> Unit,
    onNavigateToHrGraph: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp, start = 8.dp, end = 8.dp)
    ) {
        item {
            ListHeader {
                Text(text = "Experiments", style = MaterialTheme.typography.title3)
            }
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToWeather,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text("Live Weather") },
                secondaryLabel = { Text("Pre-ride conditions", color = Color.Gray) },
                icon = { Icon(Icons.Default.WbSunny, contentDescription = "Weather") }
            )
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToElevation,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text("Elevation Profile") },
                secondaryLabel = { Text("Canvas Area Chart", color = Color.Gray) },
                icon = { Icon(Icons.Default.Terrain, contentDescription = "Elevation") }
            )
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToHrGraph,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text("7-Day Heart Rate") },
                secondaryLabel = { Text("Canvas Bar Chart", color = Color.Gray) },
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Heart Rate") }
            )
        }

        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToMap,
                colors = ChipDefaults.secondaryChipColors(),
                label = { Text("Ride Map") },
                secondaryLabel = { Text("Polyline GPS Trace", color = Color.Gray) },
                icon = { Icon(Icons.Default.Map, contentDescription = "Map") }
            )
        }
    }
}