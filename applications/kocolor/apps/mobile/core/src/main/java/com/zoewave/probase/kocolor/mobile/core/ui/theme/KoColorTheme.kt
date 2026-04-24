package com.zoewave.probase.kocolor.mobile.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- CLASSIC SCHEMES ---
private val ClassicDarkColorScheme = darkColorScheme(
    primary = ClassicPrimaryDark,
    secondary = ClassicSecondaryDark,
    tertiary = ClassicTertiaryDark,
    background = ClassicBackgroundDark,
    surface = ClassicSurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val ClassicLightColorScheme = lightColorScheme(
    primary = ClassicPrimary,
    secondary = ClassicSecondary,
    tertiary = ClassicTertiary,
    background = ClassicBackgroundLight,
    surface = ClassicSurfaceLight,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// --- PASTEL SCHEMES ---
private val PastelDarkColorScheme = darkColorScheme(
    primary = PastelPrimaryDark,
    secondary = PastelSecondaryDark,
    tertiary = PastelTertiaryDark,
    background = PastelBackgroundDark,
    surface = PastelSurfaceDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val PastelLightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    secondary = PastelSecondary,
    tertiary = PastelTertiary,
    background = PastelBackgroundLight,
    surface = PastelSurfaceLight,
    onPrimary = Color(0xFF1C1B1F),
    onBackground = Color.Black,
    onSurface = Color.Black
)

// --- VIBRANT SCHEMES ---
private val VibrantDarkColorScheme = darkColorScheme(
    primary = VibrantPrimaryDark,
    secondary = VibrantSecondaryDark,
    tertiary = VibrantTertiaryDark,
    background = VibrantBackgroundDark,
    surface = VibrantSurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val VibrantLightColorScheme = lightColorScheme(
    primary = VibrantPrimary,
    secondary = VibrantSecondary,
    tertiary = VibrantTertiary,
    background = VibrantBackgroundLight,
    surface = VibrantSurfaceLight,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// --- LUXURY SCHEMES ---
private val LuxuryDarkColorScheme = darkColorScheme(
    primary = LuxuryPrimaryDark,
    secondary = LuxurySecondaryDark,
    tertiary = LuxuryTertiaryDark,
    background = LuxuryBackgroundDark,
    surface = LuxurySurfaceDark,
    onPrimary = Color.Black,
    onBackground = Color(0xFFF9F6EE),
    onSurface = Color(0xFFF9F6EE)
)

private val LuxuryLightColorScheme = lightColorScheme(
    primary = LuxuryPrimary,
    secondary = LuxurySecondary,
    tertiary = LuxuryTertiary,
    background = LuxuryBackgroundLight,
    surface = LuxurySurfaceLight,
    onPrimary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun KoColorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: String = "CLASSIC",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        palette == "DYNAMIC" -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        palette == "PASTEL" -> if (darkTheme) PastelDarkColorScheme else PastelLightColorScheme
        palette == "VIBRANT" -> if (darkTheme) VibrantDarkColorScheme else VibrantLightColorScheme
        palette == "LUXURY" -> if (darkTheme) LuxuryDarkColorScheme else LuxuryLightColorScheme
        else -> if (darkTheme) ClassicDarkColorScheme else ClassicLightColorScheme
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
