package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.IconButton
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.ToggleButton

/**
 * Glimmer equivalent of the Material 3 samples.
 * Used for one-to-one comparison in the DroidCon talk.
 */
@Composable
fun GlimmerComparisonSamples() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Glimmer (Spatial Optimized)",
            style = GlimmerTheme.typography.titleLarge,
            color = GlimmerTheme.colors.primary
        )

        // Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Buttons", style = GlimmerTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) { Text("Filled") }
                // Glimmer encourages using standard buttons for spatial clarity 
                // rather than purely aesthetic variants like 'Outlined'
                Button(onClick = { }) { Text("Elevated") }
                Button(onClick = { }) { Text("Outlined") }
            }
        }

        // Icon Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Icon Buttons", style = GlimmerTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favorite")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        }

        // Cards
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Glimmer Card", style = GlimmerTheme.typography.titleMedium)
                Text(
                    "This is an optimized Glimmer Card. Notice the spatial focus effects, automatic depth, and high-contrast styling for additive light displays.",
                    style = GlimmerTheme.typography.bodyMedium
                )
            }
        }

        // Selection Controls
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Selection Controls", style = GlimmerTheme.typography.titleSmall)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                var checked1 by remember { mutableStateOf(true) }
                ToggleButton(checked = checked1, onCheckedChange = { checked1 = it }) {
                    Text("Checkbox Equivalent")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                var checked2 by remember { mutableStateOf(false) }
                ToggleButton(checked = checked2, onCheckedChange = { checked2 = it }) {
                    Text("Switch Equivalent")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                var checked3 by remember { mutableStateOf(true) }
                ToggleButton(checked = checked3, onCheckedChange = { checked3 = it }) {
                    Text("Radio Equivalent")
                }
            }
        }

        // Slider Equivalent
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Range Input (Glimmer)", style = GlimmerTheme.typography.titleSmall)
            Text(
                "Glimmer avoids traditional sliders for precision. Use ToggleButtons or distinct steps for spatial interaction.",
                style = GlimmerTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { i ->
                    var checked by remember { mutableStateOf(i == 2) }
                    ToggleButton(checked = checked, onCheckedChange = { checked = it }) {
                        Text("${i + 1}")
                    }
                }
            }
        }

        // Color Palette
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Glimmer Color Palette (Optimized)", style = GlimmerTheme.typography.titleSmall)
            Text(
                "Glimmer colors are 'electric' and high-vibrancy. They leverage additive light physics to remain solid and legible over real-world backgrounds.",
                style = GlimmerTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GlimmerColorSwatch(GlimmerTheme.colors.primary, "Primary")
                GlimmerColorSwatch(GlimmerTheme.colors.secondary, "Secondary")
                GlimmerColorSwatch(GlimmerTheme.colors.positive, "Positive")
            }
        }
    }
}

@Composable
private fun GlimmerColorSwatch(color: androidx.compose.ui.graphics.Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, CircleShape)
        )
        Text(label, style = GlimmerTheme.typography.bodySmall)
    }
}
