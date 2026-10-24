package com.zoewave.probase.goswift.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.zoewave.probase.goswift.data.ThemeMode

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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFAC332A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFF6F61),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF855316),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFBC76),
    onSecondaryContainer = Color(0xFF2C1600),
    tertiary = Color(0xFF695F02),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB9AD4F),
    onTertiaryContainer = Color(0xFF201C00),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C21),
    surfaceVariant = Color(0xFFE1E2E9),
    onSurfaceVariant = Color(0xFF58413E),
    outline = Color(0xFF8C716D),
    outlineVariant = Color(0xFFE0BFBB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F3FB),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE6E8EF)
)

@Composable
fun GoSwiftTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
