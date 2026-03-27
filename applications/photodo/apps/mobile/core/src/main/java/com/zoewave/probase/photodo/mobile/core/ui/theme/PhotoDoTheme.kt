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

// 1. Existing Default Schemes
private val DefaultDarkColorScheme = darkColorScheme(
    primary = PhotoDoPrimaryDark,
    secondary = PhotoDoSecondaryDark,
    tertiary = PhotoDoTertiaryDark,
    background = PhotoDoBackgroundDark,
    surface = PhotoDoSurfaceDark,
)
private val DefaultLightColorScheme = lightColorScheme(
    primary = PhotoDoPrimary,
    secondary = PhotoDoSecondary,
    tertiary = PhotoDoTertiary,
    background = PhotoDoBackgroundLight,
    surface = PhotoDoSurfaceLight,
)

// 2. NEW: Coral Reef Schemes
private val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    secondary = CoralSecondaryDark,
    tertiary = CoralTertiaryDark,
    background = CoralBackgroundDark,
    surface = CoralSurfaceDark,
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
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

// 3. Updated Theme Composable
@Composable
fun PhotoDoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: String = "DEFAULT", // 🚀 NEW: Accept the palette choice!
    dynamicColor: Boolean = false, // Turn off dynamic color so your branding overrides Android 12+ wallpaper colors
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