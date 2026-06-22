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
fun IndirectPointerGestureSamples() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Indirect Pointer Demo\n(Swipe/Tap on Touchpad)", style = GlimmerTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun IndirectPointerGestureSamplesPreview() {
    GlimmerTheme {
        IndirectPointerGestureSamples()
    }
}
