package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Button
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun ButtonsSamples() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Button Samples", style = GlimmerTheme.typography.titleMedium)
        
        Button(onClick = {}) {
            Text("Primary Button")
        }
        
        Button(onClick = {}, enabled = false) {
            Text("Disabled Button")
        }
        
        Button(onClick = {}) {
            Text("Large Button Text Example", style = GlimmerTheme.typography.bodyLarge)
        }
    }
}
