package com.zoewave.probase.photodo.mobile.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
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
private val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    secondary = CoralSecondaryDark,
    tertiary = CoralTertiaryDark,
    background = CoralBackgroundDark,
    surface = CoralSurfaceDark,
    surfaceVariant = Color(0xFF1E3538), // 🚀 Heavily tinted teal cards!
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val CoralLightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    secondary = CoralSecondary,
    tertiary = CoralTertiary,
    background = CoralBackgroundLight,
    surface = CoralSurfaceLight,
    surfaceVariant = Color(0xFFD6EBE9), // 🚀 Heavily tinted teal cards!
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun PhotoDoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: String = "DEFAULT",
    content: @Composable () -> Unit
) {
    val colorScheme = when {
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