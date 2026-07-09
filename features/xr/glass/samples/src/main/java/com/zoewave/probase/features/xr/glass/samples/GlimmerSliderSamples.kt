package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.Card
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.Icon
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.surface

/**
 * Custom Glimmer Slider implementation.
 * Since the official Slider is not yet in the SDK, we build it using Surface 
 * following the design principles for AI glasses.
 */
@Composable
fun GlimmerSliderSamples() {
    var volume by remember { mutableStateOf(0.4f) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Custom Glimmer Slider", style = GlimmerTheme.typography.titleMedium)

        // The "Chroma" card from the documentation screenshot
        Card(
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Transparent",
                            style = GlimmerTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = "Chroma",
                            style = GlimmerTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Our Custom Glimmer Slider
                GlimmerSlider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun GlimmerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(48.dp)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val width = maxWidth
        val thumbWidth = 64.dp
        
        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val progress = (change.position.x / widthPx).coerceIn(0f, 1f)
                        onValueChange(progress)
                    }
                }
        )

        // Active Track
        Box(
            modifier = Modifier
                .fillMaxWidth(value)
                .height(2.dp)
                .background(Color.White, CircleShape)
        )

        // Thumb - Following Glimmer "Pill" design
        Box(
            modifier = Modifier
                .offset { 
                    IntOffset(
                        ((value * (width.toPx() - thumbWidth.toPx()))).toInt(), 
                        0
                    ) 
                }
                .size(width = thumbWidth, height = 32.dp)
                .surface(shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun GlimmerSliderSamplesPreview() {
    GlimmerTheme {
        GlimmerSliderSamples()
    }
}
