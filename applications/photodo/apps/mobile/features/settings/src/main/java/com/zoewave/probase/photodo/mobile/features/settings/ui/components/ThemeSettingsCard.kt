package com.zoewave.probase.photodo.mobile.features.settings.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.settings.R

// We keep this localized so the UI component is fully self-contained
object ThemeIdentifiers {
    const val SYSTEM = "SYSTEM"
    const val LIGHT = "LIGHT"
    const val DARK = "DARK"
}

sealed class ThemeOption(val identifier: String, @StringRes val displayResId: Int) {
    data object SystemTheme : ThemeOption(ThemeIdentifiers.SYSTEM, R.string.applications_photodo_apps_mobile_features_settings_theme_system_display)
    data object LightTheme : ThemeOption(ThemeIdentifiers.LIGHT, R.string.applications_photodo_apps_mobile_features_settings_theme_light_display)
    data object DarkTheme : ThemeOption(ThemeIdentifiers.DARK, R.string.applications_photodo_apps_mobile_features_settings_theme_dark_display)
}

private val themeOptionsList = listOf(
    ThemeOption.SystemTheme,
    ThemeOption.LightTheme,
    ThemeOption.DarkTheme
)

@Composable
fun getCurrentThemeDisplayStringRes(themeIdentifier: String): Int {
    return when (themeIdentifier) {
        ThemeIdentifiers.LIGHT -> R.string.applications_photodo_apps_mobile_features_settings_theme_light_display
        ThemeIdentifiers.DARK -> R.string.applications_photodo_apps_mobile_features_settings_theme_dark_display
        else -> R.string.applications_photodo_apps_mobile_features_settings_theme_system_display
    }
}

@Composable
fun ThemeSettingsCard(
    title: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_settings_theme_settings_content_description)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))

                // Shows current selected theme name (e.g., "Dark") next to the arrow
                Text(
                    text = stringResource(id = getCurrentThemeDisplayStringRes(currentTheme)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) {
                        stringResource(R.string.applications_photodo_apps_mobile_features_settings_collapse_content_description)
                    } else {
                        stringResource(R.string.applications_photodo_apps_mobile_features_settings_expand_content_description)
                    }
                )
            }

            if (expanded) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    themeOptionsList.forEach { themeOption ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (themeOption.identifier == currentTheme),
                                    onClick = { onThemeSelected(themeOption.identifier) }
                                )
                                .padding(vertical = 12.dp), // slightly larger touch target
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (themeOption.identifier == currentTheme),
                                onClick = { onThemeSelected(themeOption.identifier) }
                            )
                            Text(
                                text = stringResource(themeOption.displayResId),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}