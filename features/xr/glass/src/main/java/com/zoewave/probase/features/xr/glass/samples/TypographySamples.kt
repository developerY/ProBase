package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun TypographySamples() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Title Large", style = GlimmerTheme.typography.titleLarge)
        Text("Title Medium", style = GlimmerTheme.typography.titleMedium)
        Text("Title Small", style = GlimmerTheme.typography.titleSmall)
        Text("Body Large", style = GlimmerTheme.typography.bodyLarge)
        Text("Body Medium", style = GlimmerTheme.typography.bodyMedium)
        Text("Body Small", style = GlimmerTheme.typography.bodySmall)
        Text("Caption", style = GlimmerTheme.typography.caption)
    }
}
