package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.DepthEffectLevels
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun DepthEffectLevelsSample() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Depth Effect Levels", style = GlimmerTheme.typography.titleMedium)
        
        Card(
            title = { Text("Level 1") }
        ) {
            Text("Standard rest state depth.")
        }

        // Note: Glimmer Card handles its own depth, but we can simulate visual hierarchy
        Text("Level 2 is typically for focus/pressed states.")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DepthEffectLevelsSamplePreview() {
    GlimmerTheme {
        DepthEffectLevelsSample()
    }
}
