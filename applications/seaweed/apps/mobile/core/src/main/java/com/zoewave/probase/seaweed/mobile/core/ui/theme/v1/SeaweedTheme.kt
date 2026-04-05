package com.zoewave.probase.seaweed.mobile.core.ui.theme.v1

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zoewave.probase.seaweed.mobile.core.ui.theme.*
import com.zoewave.probase.seaweed.model.SeaweedThemeConfig

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
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
