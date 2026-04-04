package com.zoewave.probase.seaweed.mobile.core.ui.theme.v1

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.zoewave.probase.seaweed.mobile.core.ui.theme.SeaweedPrimary
import com.zoewave.probase.seaweed.mobile.core.ui.theme.SeaweedPrimaryDark
import com.zoewave.probase.seaweed.mobile.core.ui.theme.SeaweedSecondary
import com.zoewave.probase.seaweed.mobile.core.ui.theme.SeaweedSecondaryDark
import com.zoewave.probase.seaweed.mobile.core.ui.theme.SeaweedTertiary
import com.zoewave.probase.seaweed.mobile.core.ui.theme.SeaweedTertiaryDark
import com.zoewave.probase.seaweed.mobile.core.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = SeaweedPrimaryDark,
    secondary = SeaweedSecondaryDark,
    tertiary = SeaweedTertiaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = SeaweedPrimary,
    secondary = SeaweedSecondary,
    tertiary = SeaweedTertiary
)

@Composable
fun SeaweedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
