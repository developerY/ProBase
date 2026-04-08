package com.zoewave.probase.goswift.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

private val WearDarkColorScheme = ColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFFAED581),
    tertiary = Color(0xFFFFD54F),
    surfaceContainer = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5)
)

@Composable
fun GoSwiftWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WearDarkColorScheme,
        content = content
    )
}
