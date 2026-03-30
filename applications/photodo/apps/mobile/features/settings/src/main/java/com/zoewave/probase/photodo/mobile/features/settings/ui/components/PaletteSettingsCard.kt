package com.zoewave.probase.photodo.mobile.features.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zoewave.probase.photodo.mobile.features.settings.R

@Composable
fun PaletteSettingsCard(
    currentPalette: String,
    onPaletteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // State is safely encapsulated inside the component
    var isPaletteExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column {
            // The Header Row
            Row(
                modifier = Modifier
                    .clickable { isPaletteExpanded = !isPaletteExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_title))
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))

                // Display the current selection
                Text(
                    text = when(currentPalette) {
                        "CORAL_REEF" -> stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_coral_reef)
                        "FOREST" -> stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_deep_forest)
                        "EXPRESSIVE" -> stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_expressive_summary)
                        else -> stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_default_summary)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // The Expandable Content
            if (isPaletteExpanded) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp)) {

                    // Option 1: Default
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (currentPalette == "DEFAULT"),
                                onClick = { onPaletteSelected("DEFAULT") }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentPalette == "DEFAULT"),
                            onClick = null // Handled by the parent Row's selectable modifier
                        )
                        Text(
                            text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_default),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // Option 2: Coral Reef
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (currentPalette == "CORAL_REEF"),
                                onClick = { onPaletteSelected("CORAL_REEF") }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentPalette == "CORAL_REEF"),
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_coral_reef),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }


                    // Option 3: Deep Forest
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (currentPalette == "FOREST"),
                                onClick = { onPaletteSelected("FOREST") }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentPalette == "FOREST"),
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_deep_forest),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // Option 4: Expressive (Dynamic Wallpaper)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (currentPalette == "EXPRESSIVE"),
                                onClick = { onPaletteSelected("EXPRESSIVE") }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentPalette == "EXPRESSIVE"),
                            onClick = null
                        )
                        Text(
                            text = stringResource(R.string.applications_photodo_apps_mobile_features_settings_palette_expressive),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
