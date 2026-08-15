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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
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
        // --- LEFT SIDE: INFO NAVIGATION ---
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onInfoClick() }
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with thick colored border
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFBF8F5))
                    .border(
                        width = 4.dp,
                        color = parseHexColor(item.colorHex),
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

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp), // Slightly smaller header
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1C1E)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = { onInfoClick() },
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "i",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                val subtitleText = remember(item.brand, item.shadeName) {
                    if (!item.shadeName.isNullOrBlank()) {
                        "${item.brand} • ${item.shadeName}"
                    } else {
                        item.brand
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = subtitleText ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp), // Increased size
                        color = Color.Gray,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    // Color swatch next to name
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(item.colorHex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
                    )
                }
                
                item.calculatedUnitPrice?.let { unitPrice ->
                    Text(
                        text = "$${"%.2f".format(unitPrice)}/ml",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 13.sp, // Decreased size
                            fontWeight = FontWeight.Light
                        ),
                        color = Color(0xFF7CA682),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        // --- RIGHT SIDE: SELECTION TARGET (GLOWING) ---
        Box(
            modifier = Modifier
                .width(88.dp)
                .fillMaxHeight()
                .clickable { onSelectClick() },
            contentAlignment = Alignment.Center
        ) {
            val selectionColor = Color(0xFF745E7A)
            
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFD4AF37).copy(alpha = 0.35f),
                    Color(0xFFD4AF37).copy(alpha = 0.05f),
                    Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(glowBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.5.dp, if (isSelected) selectionColor else Color.LightGray.copy(alpha = 0.6f)),
                    shadowElevation = 2.dp
                ) {
                    if (isSelected) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(selectionColor)
                            )
                        }
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
