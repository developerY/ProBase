package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackPreviewTopAppBar(
    onBack: () -> Unit,
    onSelectAll: () -> Unit,
    onClear: () -> Unit,
    onWipe: () -> Unit,
    isWipeVisible: Boolean = false
) {
    val serifFont = FontFamily.Serif

    CenterAlignedTopAppBar(
        title = {
            Column {
                Text(
                    text = "KoColor Boutique",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select Items",
                    fontFamily = serifFont,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (isWipeVisible) {
                IconButton(onClick = onWipe) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Wipe",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            TextButton(onClick = onSelectAll) {
                Text("Select All", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = onClear) {
                Text("Clear", style = MaterialTheme.typography.labelMedium)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PackPreviewTopAppBarPreview() {
    MaterialTheme {
        PackPreviewTopAppBar(
            onBack = {},
            onSelectAll = {},
            onClear = {},
            onWipe = {},
            isWipeVisible = true
        )
    }
}
