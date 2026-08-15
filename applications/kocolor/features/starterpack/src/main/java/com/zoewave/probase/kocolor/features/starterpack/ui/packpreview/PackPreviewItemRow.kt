package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.rememberBlurHashPainter
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import kotlinx.coroutines.delay

@Composable
fun PackPreviewItemRow(
    item: PackItemDto,
    isSelected: Boolean,
    isTarget: Boolean,
    onInfoClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    var highlightActive by remember { mutableStateOf(isTarget) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (highlightActive) Color(0xFF745E7A).copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 1000),
        label = "highlight"
    )

    LaunchedEffect(isTarget) {
        if (isTarget) {
            highlightActive = true
            delay(2000)
            highlightActive = false
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val itemColor = parseHexColor(item.colorHex)

        // --- LEFT SIDE: INFO NAVIGATION ---
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onInfoClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with GLOWING border
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow Layer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    itemColor.copy(alpha = 0.4f),
                                    itemColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                )

                // Main Image Box
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFBF8F5))
                        .border(
                            width = 4.dp,
                            color = itemColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    val placeholder = rememberBlurHashPainter(blurHash = item.blurhash)

                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = null,
                        placeholder = placeholder,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E)
                )
                
                val subtitleText = remember(item.brand, item.shadeName) {
                    if (!item.shadeName.isNullOrBlank()) {
                        "${item.brand} • ${item.shadeName}"
                    } else {
                        item.brand
                    }
                }
                
                Text(
                    text = subtitleText ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = Color.Gray.copy(alpha = 0.8f),
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                item.calculatedUnitPrice?.let { unitPrice ->
                    Text(
                        text = "$${"%.2f".format(unitPrice)}/ml",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF7CA682),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // --- LINE BREAK (Vertical Divider) ---
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp),
            thickness = 1.dp,
            color = Color.Black.copy(alpha = 0.1f)
        )

        // --- RIGHT SIDE: SELECTION TARGET (Golden Glow) ---
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .background(Color(0xFFF7F7F7))
                .clickable { onSelectClick() },
            contentAlignment = Alignment.Center
        ) {
            // Glowing Halo background
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFD4AF37).copy(alpha = if (isSelected) 0.5f else 0.25f),
                    Color(0xFFD4AF37).copy(alpha = 0.05f),
                    Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(glowBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFD4AF37) else Color.LightGray.copy(alpha = 0.4f)),
                    shadowElevation = if (isSelected) 4.dp else 1.dp
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFD4AF37).copy(alpha = 0.2f), Color.Transparent)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun PackPreviewItemRowPreview() {
    MaterialTheme {
        PackPreviewItemRow(
            item = CosmeticItemDto(
                id = "1",
                name = "KoColor Purifying Gel Cleanser",
                brand = "KoColor",
                shadeName = "Clear Crystal",
                colorHex = "#F4F6F0",
                thumbnailUrl = "",
                imageUrl = "",
                blurhash = "LEHV6nWB2yk8pyo0adRj00WBof%M",
                macroCategory = "PREP",
                microCategory = "CLEANSER",
                price = 18.0,
                notes = null,
                formulation = "GEL",
                chemistryBase = "WATER",
                finish = "NATURAL",
                coverage = "SHEER",
                temperature = "NEUTRAL",
                volume = "150ml",
                paoMonths = 12,
                expiryDate = null,
                instructions = null,
                ingredients = emptyList(),
                allergens = emptyList(),
                isVegan = true,
                isCrueltyFree = true,
                fdaDataVerified = true
            ),
            isSelected = true,
            isTarget = false,
            onInfoClick = {},
            onSelectClick = {}
        )
    }
}
