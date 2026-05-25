package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface

@Composable
fun SurfaceSamples() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .surface()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("This container uses the .surface() modifier.", style = GlimmerTheme.typography.titleMedium)
    }
}
