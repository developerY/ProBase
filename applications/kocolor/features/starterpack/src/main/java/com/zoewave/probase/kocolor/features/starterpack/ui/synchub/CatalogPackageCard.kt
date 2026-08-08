package com.zoewave.probase.kocolor.features.starterpack.ui.synchub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo

@Composable
fun CatalogPackageCard(
    pack: PackInfo,
    status: PackStatus,
    onImportClick: () -> Unit,
    onWipeClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val serifFont = FontFamily.Serif
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column {
            // Thumbnail
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
            ) {
                AsyncImage(
                    model = pack.heroImageUrl ?: "https://cdn.kocolor.com/inventory/assets/${pack.id}.webp",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                if (status == PackStatus.INSTALLED) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp).padding(2.dp), tint = Color(0xFF4CAF50))
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = serifFont,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "• ${pack.itemCount} Items",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onImportClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5A3854)),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Text(if (status == PackStatus.INSTALLED) "PREVIEW" else "IMPORT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    if (status == PackStatus.INSTALLED) {
                        IconButton(
                            onClick = onWipeClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogPackageCardPreview() {
    MaterialTheme {
        CatalogPackageCard(
            pack = PackInfo(
                id = "winter",
                name = "Winter 2026 Trend Kit",
                description = "Curated seasonal picks for Winter color profiles.",
                version = 1,
                publisher = "KoColor",
                packType = "SAMPLE_PACK",
                endpoint = "winter.json",
                itemCount = 3,
                compressedSizeBytes = 512,
                uncompressedSizeBytes = 1024,
                sha256 = "hash",
                signature = "sig",
                compressionAlgorithm = "zstd",
                hashAlgorithm = "sha256",
                hashEncoding = "hex",
                signatureAlgorithm = "ed25519",
                signatureEncoding = "hex",
                packageFormatVersion = 1,
                schemaVersion = 2,
                encryption = "none"
            ),
            status = PackStatus.AVAILABLE,
            onImportClick = {},
            onWipeClick = {},
            isLoading = false
        )
    }
}
