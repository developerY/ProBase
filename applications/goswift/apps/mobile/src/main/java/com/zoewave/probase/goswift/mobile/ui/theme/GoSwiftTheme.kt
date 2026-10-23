package com.zoewave.probase.goswift.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceContainerHigh,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    error = error,
    onError = onError,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh
)

@Composable
fun GoSwiftTheme(
    content: @Composable () -> Unit
) {
    // Warm Biometric Telemetry is strictly Dark Theme
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
