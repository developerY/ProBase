package com.zoewave.probase.photodo.mobile.features.settings.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemeSettingsCardAI(
    title: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onExpandToggle,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            ListItem(
                headlineContent = { Text(title) },
                supportingContent = { Text("Current: $currentTheme") },
                trailingContent = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            )
            if (expanded) {
                val themes = listOf(
                    ThemeIdentifiers.SYSTEM,
                    ThemeIdentifiers.LIGHT,
                    ThemeIdentifiers.DARK
                )
                themes.forEach { theme ->
                    ListItem(
                        headlineContent = { Text(theme) },
                        trailingContent = {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = { onThemeSelected(theme) }
                            )
                        }
                    )
                }
            }
        }
    }
}