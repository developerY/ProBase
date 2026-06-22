package com.zoewave.probase.features.xr.glass.samples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.xr.glimmer.GlimmerTheme
import androidx.xr.glimmer.ListItem
import androidx.xr.glimmer.Text
import androidx.xr.glimmer.list.GlimmerLazyColumn

/**
 * Samples demonstrating Glimmer Typography vs. Standard Material Compose Typography.
 *
 * This teaching tool shows how standard Material components can fail on additive glass displays
 * due to contrast and legibility issues, while Glimmer components are optimized for XR.
 *
 * NOTE: Items are wrapped in [ListItem] to make them focusable, which is required for
 * scrolling in the Glass interaction model.
 */
@Composable
fun TypographySamples() {
    GlimmerLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Mandatory black background for additive displays
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp) // Glimmer standard spacing
    ) {
        item {
            Text(
                text = "--- Correct Glimmer Typography ---",
                style = GlimmerTheme.typography.titleMedium,
                color = GlimmerTheme.colors.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        item { ListItem(onClick = {}) { Text("Title Large", style = GlimmerTheme.typography.titleLarge) } }
        item { ListItem(onClick = {}) { Text("Title Medium", style = GlimmerTheme.typography.titleMedium) } }
        item { ListItem(onClick = {}) { Text("Title Small", style = GlimmerTheme.typography.titleSmall) } }
        item { ListItem(onClick = {}) { Text("Body Large", style = GlimmerTheme.typography.bodyLarge) } }
        item { ListItem(onClick = {}) { Text("Body Medium", style = GlimmerTheme.typography.bodyMedium) } }
        item { ListItem(onClick = {}) { Text("Body Small", style = GlimmerTheme.typography.bodySmall) } }
        item { ListItem(onClick = {}) { Text("Caption", style = GlimmerTheme.typography.caption) } }

        item {
            Text(
                text = "--- Common Mistakes (Material 3) ---",
                style = GlimmerTheme.typography.titleMedium,
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Mistake 1: Default Material Text (Too dark/transparent on glass)
        item {
            ListItem(onClick = {}) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Glimmer Body Medium", style = GlimmerTheme.typography.bodyMedium)
                    androidx.compose.material3.Text(
                        text = "Mistake: Default Material Text (Invisible on Glass)",
                        style = androidx.compose.material3.Typography().bodyMedium
                        // Note: No color specified, defaults to dark text which is transparent on additive displays
                    )
                }
            }
        }

        // Mistake 2: Too Thin/Small
        item {
            ListItem(onClick = {}) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Glimmer Caption (18sp, Bold)", style = GlimmerTheme.typography.caption)
                    androidx.compose.material3.Text(
                        text = "Mistake: Too small/thin (Alias/Shimmer issues)",
                        color = Color.White,
                        style = androidx.compose.material3.Typography().labelSmall
                    )
                }
            }
        }

        // Comparison 3: Proper Weight
        item {
            ListItem(onClick = {}) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Glimmer Title Large (Bold, Optimal Contrast)", style = GlimmerTheme.typography.titleLarge)
                    androidx.compose.material3.Text(
                        text = "Compose Title Large (Forced White)",
                        color = Color.White,
                        style = androidx.compose.material3.Typography().titleLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TypographySamplesPreview() {
    GlimmerTheme {
        TypographySamples()
    }
}
