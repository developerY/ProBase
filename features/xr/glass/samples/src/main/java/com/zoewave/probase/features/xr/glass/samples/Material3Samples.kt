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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Standard Material 3 components using the default M3 Dark Theme.
 * This is used to demonstrate how "traditional" mobile UI renders on Glass 
 * compared to the optimized Glimmer toolkit.
 */
@Composable
fun Material3Samples() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Standard Material 3",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Buttons", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { }) { Text("Filled") }
                        ElevatedButton(onClick = { }) { Text("Elevated") }
                        OutlinedButton(onClick = { }) { Text("Outlined") }
                    }
                }

                // Icon Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Icon Buttons", style = MaterialTheme.typography.titleSmall)
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
                        Text("Material 3 Card", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "This is a standard M3 Card component. Notice the lack of spatial depth effects compared to Glimmer.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Selection Controls
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selection Controls", style = MaterialTheme.typography.titleSmall)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var checked by remember { mutableStateOf(true) }
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        Text("Checkbox")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var switched by remember { mutableStateOf(false) }
                        Switch(checked = switched, onCheckedChange = { switched = it })
                        Text("Switch", modifier = Modifier.padding(start = 8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = true, onClick = { })
                        Text("Radio Button")
                    }
                }

                // Slider
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Slider", style = MaterialTheme.typography.titleSmall)
                    var sliderPosition by remember { mutableStateOf(0.5f) }
                    Slider(value = sliderPosition, onValueChange = { sliderPosition = it })
                }

                // Color Palette
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("M3 Color Palette (Mobile)", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Standard M3 colors use tonal palettes designed for reflective screens. On see-through glass, these can appear 'muddy' or transparent.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ColorSwatch(MaterialTheme.colorScheme.primary, "Primary")
                        ColorSwatch(MaterialTheme.colorScheme.secondary, "Secondary")
                        ColorSwatch(MaterialTheme.colorScheme.tertiary, "Tertiary")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: androidx.compose.ui.graphics.Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, CircleShape)
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
