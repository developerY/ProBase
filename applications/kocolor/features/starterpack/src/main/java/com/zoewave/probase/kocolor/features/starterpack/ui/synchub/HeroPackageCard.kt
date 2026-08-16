package com.zoewave.probase.kocolor.features.starterpack.ui.synchub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.rememberBlurHashPainter
import com.zoewave.probase.kocolor.db.entity.PackStatus
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo

@Composable
fun HeroPackageCard(
    pack: PackInfo,
    status: PackStatus,
    onImportClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val serifFont = FontFamily.Serif
    val plumColor = Color(0xFF5A3854)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image with BlurHash placeholder
            val placeholder = rememberBlurHashPainter(
                blurHash = pack.heroBlurHash,
                fallbackColor = Color.LightGray.copy(alpha = 0.1f)
            )

            AsyncImage(
                model = pack.heroImageUrl ?: "https://cdn.kocolor.com/inventory/dist/assets/hero/kc-prep-01.webp",
                contentDescription = null,
                placeholder = placeholder,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Info Button (mockup image_4c0f3c.jpg top right of hero)
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Black)
            }

            // Content Card
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(0.85f),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = serifFont,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${pack.description} • ${pack.itemCount} Items",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = plumColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (status == PackStatus.INSTALLED) "PREVIEW COLLECTION" else "DOWNLOAD COLLECTION",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            
            // Security Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    // Shortened to just "Verified"
                    Spacer(Modifier.width(4.dp))
                    Text("Verified", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeroPackageCardPreview() {
    MaterialTheme {
        HeroPackageCard(
            pack = PackInfo(
                id = "core",
                name = "Core Collection",
                description = "Curated high-fidelity kits to power your AI styling.",
                version = 1,
                publisher = "KoColor Official",
                packType = "STARTER_PACK",
                endpoint = "core.json",
                itemCount = 9,
                compressedSizeBytes = 1024,
                uncompressedSizeBytes = 2048,
                sha256 = "hash",
                signature = "sig",
                compressionAlgorithm = "zstd",
                hashAlgorithm = "sha256",
                hashEncoding = "hex",
                signatureAlgorithm = "ed25519",
                signatureEncoding = "hex",
                packageFormatVersion = 1,
                schemaVersion = 1,
                encryption = "none"
            ),
            status = PackStatus.AVAILABLE,
            onImportClick = {},
            onInfoClick = {}
        )
    }
}
