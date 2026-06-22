package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon

@Composable
fun IconSamples() {
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = GlimmerTheme.colors.primary)
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GlimmerTheme.colors.secondary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun IconSamplesPreview() {
    GlimmerTheme {
        IconSamples()
    }
}
