package com.zoewave.probase.seaweed.mobile.core.ui.theme.v1

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zoewave.probase.seaweed.mobile.core.ui.theme.*
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig
import com.zoewave.probase.seaweed.model.ThemeMode

private val SeaweedDarkColorScheme = darkColorScheme(
    primary = SeaweedPrimaryDark,
    secondary = SeaweedSecondaryDark,
    tertiary = SeaweedTertiaryDark
)

private val SeaweedLightColorScheme = lightColorScheme(
    primary = SeaweedPrimary,
    secondary = SeaweedSecondary,
    tertiary = SeaweedTertiary
)

private val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    secondary = CoralSecondaryDark,
    tertiary = CoralTertiaryDark
)

private val CoralLightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    secondary = CoralSecondary,
    tertiary = CoralTertiary
)

@Composable
fun SeaweedTheme(
    themeConfig: SeaweedThemeConfig = SeaweedThemeConfig.DEFAULT,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Default to false to respect custom theme selections
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeConfig == SeaweedThemeConfig.CORAL -> if (darkTheme) CoralDarkColorScheme else CoralLightColorScheme
        else -> if (darkTheme) SeaweedDarkColorScheme else SeaweedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
