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
import androidx.compose.ui.unit.dp

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
                Icon(Icons.Default.Palette, contentDescription = "Color Palette")
                Spacer(modifier = Modifier.width(16.dp))
                Text("Color Palette", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))

                // Display the current selection
                Text(
                    text = when(currentPalette) {
                        "CORAL_REEF" -> "Coral Reef"
                        "FOREST" -> "Deep Forest"
                        "EXPRESSIVE" -> "Your Wallpaper"
                        else -> "Default"
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
                            text = "Default Professional",
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
                            text = "Coral Reef",
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
                            text = "Deep Forest",
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
                            text = "Material 3 Expressive (Wallpaper)",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}