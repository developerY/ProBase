package com.zoewave.probase.photodo.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

// Photodo Primary Colors
val PhotoDoPrimary = Color(0xFF5C5E7A)
val PhotoDoSecondary = Color(0xFFB5EAD7)
val PhotoDoTertiary = Color(0xFFFFB4A2)

private val WearColorScheme = ColorScheme(
    primary = PhotoDoPrimary,
    onPrimary = Color.Black,
    secondary = PhotoDoSecondary,
    onSecondary = Color.Black,
    tertiary = PhotoDoTertiary,
    onTertiary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    error = Color(0xFFFFB4A2),
    onError = Color.Black
)

@Composable
fun PhotoDoWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WearColorScheme,
        content = content
    )
}
