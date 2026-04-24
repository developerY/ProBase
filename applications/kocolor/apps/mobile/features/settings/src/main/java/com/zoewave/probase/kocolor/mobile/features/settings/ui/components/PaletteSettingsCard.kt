package com.zoewave.probase.kocolor.mobile.features.settings.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
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

object PaletteIdentifiers {
    const val CLASSIC = "CLASSIC"
    const val PASTEL = "PASTEL"
    const val VIBRANT = "VIBRANT"
    const val LUXURY = "LUXURY"
    const val DYNAMIC = "DYNAMIC"
}

private val paletteOptions = listOf(
    PaletteIdentifiers.CLASSIC to R.string.applications_kocolor_apps_mobile_core_palette_classic,
    PaletteIdentifiers.PASTEL to R.string.applications_kocolor_apps_mobile_core_palette_pastel,
    PaletteIdentifiers.VIBRANT to R.string.applications_kocolor_apps_mobile_core_palette_vibrant,
    PaletteIdentifiers.LUXURY to R.string.applications_kocolor_apps_mobile_core_palette_luxury,
    PaletteIdentifiers.DYNAMIC to R.string.applications_kocolor_apps_mobile_core_palette_dynamic
)

@Composable
fun PaletteSettingsCard(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    currentPalette: String,
    onPaletteSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ColorLens, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_core_settings_palette_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = paletteOptions.find { it.first == currentPalette }?.second?.let { stringResource(it) } ?: "",
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
                    paletteOptions.forEach { (id, labelRes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentPalette == id,
                                    onClick = { onPaletteSelected(id) }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentPalette == id,
                                onClick = { onPaletteSelected(id) }
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

@Preview(showBackground = true)
@Composable
private fun PaletteSettingsCardPreview() {
    MaterialTheme {
        PaletteSettingsCard(
            expanded = true,
            onExpandToggle = {},
            currentPalette = PaletteIdentifiers.CLASSIC,
            onPaletteSelected = {}
        )
    }
}
