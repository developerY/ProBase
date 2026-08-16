package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.ui.util.PremiumProductImage
import com.zoewave.probase.core.ui.util.parseColor
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
        val itemColor = parseColor(item.colorHex)

        // --- LEFT SIDE: INFO NAVIGATION ---
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onInfoClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with IMMERSIVE VIBRANT GLOW
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(104.dp), // Slightly larger container for wide glow
                contentAlignment = Alignment.Center
            ) {
                // Multi-layered Radial Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                0.0f to itemColor.copy(alpha = 0.6f),
                                0.5f to itemColor.copy(alpha = 0.2f),
                                1.0f to Color.Transparent,
                                center = Offset.Unspecified,
                                radius = Float.POSITIVE_INFINITY,
                                tileMode = TileMode.Clamp
                            ),
                            shape = CircleShape
                        )
                        .alpha(0.8f)
                )

                // Main Image Box with high-fidelity chromatic border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(itemColor.copy(alpha = 0.1f)) // Show product tint while loading
                        .border(
                            width = 4.dp,
                            color = itemColor,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    PremiumProductImage(
                        imageUrl = item.thumbnailUrl,
                        blurHash = item.blurhash,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        fallbackColor = itemColor
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
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
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = subtitleText ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = Color.Gray.copy(alpha = 0.8f),
                        letterSpacing = 0.2.sp
                    )
                    
                    Spacer(Modifier.width(10.dp))
                    
                    // We do not want to use this for now.
                    // Just a placeholder for now.
                    // Do not remove
                    /* --- THE LEFT CIRCLE (3D COLOR SWATCH) ---
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = itemColor,
                        shadowElevation = 8.dp, // Increased elevation for stronger 3D feel
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        0.0f to Color.White.copy(alpha = 0.4f),
                                        0.5f to Color.Transparent,
                                        1.0f to Color.Black.copy(alpha = 0.15f)
                                    )
                                )
                        )
                    } */
                }
                
                item.calculatedUnitPrice?.let { unitPrice ->
                    Text(
                        text = "$${"%.2f".format(unitPrice)}/ml",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = Color(0xFF7CA682),
                        modifier = Modifier.padding(top = 6.dp)
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

        // --- RIGHT SIDE: SELECTION TARGET (Item-Tinted Interaction Zone) ---
        Box(
            modifier = Modifier
                .width(59.dp)
                .fillMaxHeight()
                .background(itemColor.copy(alpha = 0.05f)) // Increased tint for better item-matching
                .clickable { onSelectClick() },
            contentAlignment = Alignment.Center
        ) {
            // Dynamic Glowing Halo based on item color (Replacing Gold)
            val glowBrush = Brush.radialGradient(
                colors = listOf(
                    itemColor.copy(alpha = if (isSelected) 0.5f else 0.15f),
                    itemColor.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(glowBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // 3D Effect Surface (Selection Circle)
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) itemColor else itemColor.copy(alpha = 0.15f) // Subtle item tint for unchecked
                    ),
                    shadowElevation = if (isSelected) 6.dp else 2.dp
                ) {
                    // Subtle inner gradient for 3D pillowed look
                    val buttonGradient = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFF9F9F9))
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(buttonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                itemColor, 
                                                itemColor.copy(alpha = 0.8f) // High-fidelity item color gradient
                                            )
                                        )
                                    )
                                    .shadow(2.dp, CircleShape)
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
