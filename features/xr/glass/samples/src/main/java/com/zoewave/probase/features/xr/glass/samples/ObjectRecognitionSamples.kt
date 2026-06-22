package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface

/**
 * Glimmer UI for Object Recognition.
 * 
 * In optical see-through displays, we only draw the overlays.
 * The user already sees the real world through the lenses.
 */
@Composable
fun ObjectRecognitionScreen(detectedObject: String? = "Coffee Mug") {
    // Empty state - the user just sees reality through the lenses
    if (detectedObject == null) return

    GlimmerTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            // Align to the top of the user's field of view
            contentAlignment = Alignment.TopCenter 
        ) {
            // Box with surface() modifier handles AR-optimized background
            Box(
                modifier = Modifier
                    .padding(top = 64.dp)
                    .surface()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Detected: $detectedObject",
                    color = Color.White,
                    style = GlimmerTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview(
    name = "Object Recognition - Light Environment",
    showBackground = true,
    backgroundColor = 0xFFCCCCCC // Simulating a bright, daytime real-world background
)
@Preview(
    name = "Object Recognition - Dark Environment",
    showBackground = true,
    backgroundColor = 0xFF333333 // Simulating a dark/nighttime environment
)
@Composable
private fun ObjectRecognitionScreenPreview() {
    GlimmerTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ObjectRecognitionScreen(detectedObject = "Coffee Mug")
        }
    }
}
