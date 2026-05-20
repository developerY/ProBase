package com.zoewave.probase.kocolor.mobile.features.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.mobile.core.R
import com.zoewave.probase.kocolor.mobile.features.settings.ui.SettingsEvent
import com.zoewave.probase.kocolor.model.KoColorRoute

object ThemeIdentifiers {
    const val SYSTEM = "SYSTEM"
    const val LIGHT = "LIGHT"
    const val DARK = "DARK"
}

private val themeOptions = listOf(
    ThemeIdentifiers.SYSTEM to R.string.applications_kocolor_apps_mobile_core_theme_system,
    ThemeIdentifiers.LIGHT to R.string.applications_kocolor_apps_mobile_core_theme_light,
    ThemeIdentifiers.DARK to R.string.applications_kocolor_apps_mobile_core_theme_dark
)

@Preview(showBackground = true)
@Composable
private fun ThemeSettingsCardPreview() {
    MaterialTheme {
        ThemeSettingsCard(
            uiState = true to ThemeIdentifiers.SYSTEM,
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun ThemeSettingsCard(
    uiState: Pair<Boolean, String>,
    onEvent: (SettingsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val expanded = uiState.first
    val currentTheme = uiState.second

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onEvent(SettingsEvent.OnThemeExpandedToggled(!expanded)) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_core_settings_theme_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = themeOptions.find { it.first == currentTheme }?.second?.let { stringResource(it) } ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    themeOptions.forEach { (id, labelRes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentTheme == id,
                                    onClick = { onEvent(SettingsEvent.OnThemeSelected(id)) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == id,
                                onClick = { onEvent(SettingsEvent.OnThemeSelected(id)) }
                            )
                            Text(
                                text = stringResource(labelRes),
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
