package com.zoewave.probase.photodo.mobile.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- DEFAULT SCHEMES ---
private val DefaultDarkColorScheme = darkColorScheme(
    primary = PhotoDoPrimaryDark,
    secondary = PhotoDoSecondaryDark,
    tertiary = PhotoDoTertiaryDark,
    background = PhotoDoBackgroundDark,
    surface = PhotoDoSurfaceDark,
    surfaceVariant = Color(0xFF2A2A2A), // Standard dark gray cards
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = PhotoDoPrimary,
    secondary = PhotoDoSecondary,
    tertiary = PhotoDoTertiary,
    background = PhotoDoBackgroundLight,
    surface = PhotoDoSurfaceLight,
    surfaceVariant = Color(0xFFF0F0F0), // Standard light gray cards
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// --- CORAL REEF SCHEMES ---
// 1. ADD THE SURFACES to your Coral Dark Scheme
private val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    secondary = CoralSecondaryDark,
    tertiary = CoralTertiaryDark,
    background = CoralBackgroundDark,
    surface = CoralSurfaceDark,
    // 🚀 THIS IS THE MAGIC BULLET FOR CARDS:
    surfaceVariant = Color(0xFF1E3538), // Heavily tinted ocean teal for cards
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFE2E2E2) // Ensure text on the cards is white
)

// 2. ADD THE SURFACES to your Coral Light Scheme
private val CoralLightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    secondary = CoralSecondary,
    tertiary = CoralTertiary,
    background = CoralBackgroundLight,
    surface = CoralSurfaceLight,
    // 🚀 THIS IS THE MAGIC BULLET FOR CARDS:
    surfaceVariant = Color(0xFFD6EBE9), // Heavily tinted teal for cards
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF1C1B1F) // Ensure text on the cards is black
)

val ForestLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackgroundLight,
    surface = ForestSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1C19),
    onSurface = Color(0xFF1A1C19)
)

val ForestDarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    secondary = ForestSecondaryDark,
    tertiary = ForestTertiaryDark,
    background = ForestBackgroundDark,
    surface = ForestSurfaceDark,
    onPrimary = Color(0xFF1B3716),
    onSecondary = Color(0xFF312C00),
    onTertiary = Color(0xFF003544),
    onBackground = Color(0xFFE2E3DD),
    onSurface = Color(0xFFE2E3DD)
)

@Composable
fun PhotoDoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: String = "DEFAULT",
    content: @Composable () -> Unit
) {
    // 🚀 Completely remove the "dynamicColor" logic from this WHEN block.
    // If dynamicColor is left in, Android ignores your background/surface colors!
    val colorScheme = when {
        palette == "FOREST" && darkTheme -> ForestDarkColorScheme
        palette == "FOREST" && !darkTheme -> ForestLightColorScheme
        palette == "CORAL_REEF" && darkTheme -> CoralDarkColorScheme
        palette == "CORAL_REEF" && !darkTheme -> CoralLightColorScheme
        darkTheme -> DefaultDarkColorScheme
        else -> DefaultLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}