package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.VoiceInputIndicator

@Composable
fun VoiceInputIndicatorSamples(level: () -> Float) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Voice Input Indicator", style = GlimmerTheme.typography.titleMedium)
        
        VoiceInputIndicator(
            level = level,
            indicatorColor = GlimmerTheme.colors.primary
        )
        
        Text("Visualizes real-time audio input levels.")
    }
}
