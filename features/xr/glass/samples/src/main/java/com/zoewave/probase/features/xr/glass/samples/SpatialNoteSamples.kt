package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin

/**
 * A Glimmer-optimized UI for a Spatial Note (Virtual Sticky Note).
 *
 * This note is anchored in 3D space. Glimmer ensures legibility
 * against various real-world backgrounds (fridge, door, etc).
 */
@Composable
fun SpatialNoteOverlay(
    noteText: String = "Don't forget to grab the house keys!",
    author: String = "Gemini"
) {
    GlimmerTheme {
        // Box with surface() modifier handles AR-optimized background
        Box(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 300.dp)
                .surface()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = GlimmerTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spatial Note",
                        style = GlimmerTheme.typography.titleSmall,
                        color = GlimmerTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = noteText,
                    color = Color.White,
                    style = GlimmerTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Left by $author",
                    color = Color.White.copy(alpha = 0.6f),
                    style = GlimmerTheme.typography.bodySmall
                )
            }
        }
    }
}

// 1. Simulating looking at the note against a bright wall
@Preview(
    name = "Spatial Note - Bright Wall",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5
)
// 2. Simulating looking at the note in a dark hallway
@Preview(
    name = "Spatial Note - Dark Hallway",
    showBackground = true,
    backgroundColor = 0xFF121212
)
@Composable
private fun SpatialNoteOverlayPreview() {
    GlimmerTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SpatialNoteOverlay()
        }
    }
}
