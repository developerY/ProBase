package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun ColorsSamples() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Glimmer Colors", style = GlimmerTheme.typography.titleMedium)
        
        ColorRow("Primary", GlimmerTheme.colors.primary)
        ColorRow("Secondary", GlimmerTheme.colors.secondary)
        ColorRow("Surface", GlimmerTheme.colors.surface)
        ColorRow("Outline", GlimmerTheme.colors.outline)
    }
}

@Composable
private fun ColorRow(name: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color)
        )
        Spacer(Modifier.width(16.dp))
        Text(name, style = GlimmerTheme.typography.bodyLarge)
    }
}
