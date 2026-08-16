package com.zoewave.probase.kocolor.features.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.rememberBlurHashPainter
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.util.toComposeColor
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

@Composable
fun WardrobeCard(
    uiState: ClothingItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColor = uiState.dominantHex ?: uiState.colorHex ?: "#FFFFFF"
    val bgColor = itemColor.toComposeColor()
    val isDark = isColorDark(bgColor)
    val contentColor = if (isDark) Color.White else Color.Black

    ElevatedCard(
        modifier = modifier.clickable { navTo(KoColorRoute.WardrobeDetail(uiState.internalId)) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.imageUrl != null) {
                val placeholder = rememberBlurHashPainter(
                    blurHash = uiState.blurhash,
                    fallbackColor = bgColor.copy(alpha = 0.1f)
                )
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = null,
                    placeholder = placeholder,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Representative Color Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                        .align(Alignment.TopEnd),
                    color = bgColor,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {}
            } else {
                Icon(
                    Icons.Default.Checkroom,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).align(Alignment.Center).alpha(0.1f),
                    tint = contentColor
                )
            }

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.brand?.uppercase() ?: stringResource(R.string.applications_kocolor_features_inventory_brand_default),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = uiState.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeCardPreview() {
    MaterialTheme {
        WardrobeCard(
            uiState = ClothingItem(name = "T-Shirt", brand = "Sample", category = ClothingCategory.TOPS, colorHex = "#FF0000"),
            onEvent = {},
            navTo = {}
        )
    }
}
