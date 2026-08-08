package com.zoewave.probase.kocolor.features.starterpack.ui.synchub

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PreviewItem

@Composable
fun PackageInfoDialog(
    pack: PackInfo,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 600.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
            tonalElevation = 12.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pack.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Collection Contents:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.offset(x = 12.dp, y = (-12).dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable list of items
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pack.previewItems) { item ->
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "- ${item.description}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                if (pack.previewItems.isEmpty()) {
                    Text(
                        "No item details available for this preview.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Total items: ${pack.itemCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PackageInfoDialogPreview() {
    MaterialTheme {
        PackageInfoDialog(
            pack = PackInfo(
                id = "core",
                name = "Core Collection",
                description = "Essentials",
                version = 1,
                publisher = "KoColor",
                packType = "STARTER_PACK",
                endpoint = "",
                itemCount = 9,
                compressedSizeBytes = 0,
                uncompressedSizeBytes = 0,
                sha256 = "",
                signature = "",
                compressionAlgorithm = "",
                hashAlgorithm = "",
                hashEncoding = "",
                signatureAlgorithm = "",
                signatureEncoding = "",
                packageFormatVersion = 1,
                schemaVersion = 1,
                encryption = "",
                previewItems = listOf(
                    PreviewItem("Iconic Red Lipstick", "Signature Crimson • SATIN • FULL COVERAGE"),
                    PreviewItem("Luminescent C Serum", "Luminous Glow • RADIANT • SHEER COVERAGE")
                )
            ),
            onDismiss = {}
        )
    }
}
