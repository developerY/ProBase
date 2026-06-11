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

/**
 * A Glimmer-optimized screen for live translation.
 * 
 * Black pixels are transparent on optical see-through displays.
 * GlimmerTheme ensures high contrast and safe padding for the user's field of view.
 */
@Composable
fun GlassesTranslationScreen(translatedText: String = "Listening for audio...") {
    GlimmerTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            // Pin the subtitles to the bottom of the user's field of view
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = translatedText,
                // Glimmer provides legibility-optimized typography (Google Sans Flex)
                style = GlimmerTheme.typography.titleLarge,
                // High contrast color optimized for display glasses
                color = GlimmerTheme.colors.primary,
                modifier = Modifier.padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}
