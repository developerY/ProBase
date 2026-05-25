package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun ShapesSamples() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Glimmer Shapes", style = GlimmerTheme.typography.titleMedium)
        
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(GlimmerTheme.shapes.small)
                    .background(GlimmerTheme.colors.secondary)
            ) {
                Text("Small", modifier = Modifier.padding(4.dp))
            }
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(GlimmerTheme.shapes.medium)
                    .background(GlimmerTheme.colors.primary)
            ) {
                Text("Medium", modifier = Modifier.padding(4.dp))
            }
        }
    }
}
