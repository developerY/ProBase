package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text

@Composable
fun GlimmerPagerSamples() {
    // Note: If Glimmer Pager is not available in the library yet, we show a placeholder
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pager Sample Placeholder", style = GlimmerTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlimmerPagerSamplesPreview() {
    GlimmerTheme {
        GlimmerPagerSamples()
    }
}
