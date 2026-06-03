package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxonomyDropdown(
    current: String,
    items: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.White
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { label ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    onSelect(label)
                    expanded = false
                })
            }
        }
    }
}
