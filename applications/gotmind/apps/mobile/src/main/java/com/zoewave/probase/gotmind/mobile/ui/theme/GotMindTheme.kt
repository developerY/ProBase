package com.zoewave.probase.gotmind.mobile.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.zoewave.probase.gotmind.model.AppTheme
import com.zoewave.probase.gotmind.model.ColorPalette

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6650a4),
    secondary = Color(0xFF625b71),
    tertiary = Color(0xFF7D5260)
)

@Composable
fun GotMindTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    palette: ColorPalette = ColorPalette.DEFAULT,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when (palette) {
        ColorPalette.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
        ColorPalette.CORAL -> if (darkTheme) CoralDarkColors else CoralLightColors
        ColorPalette.FOREST -> if (darkTheme) ForestDarkColors else ForestLightColors
        ColorPalette.OCEAN -> if (darkTheme) OceanDarkColors else OceanLightColors
        ColorPalette.MATERIAL_EXPRESSIVE -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
