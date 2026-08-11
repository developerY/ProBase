package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import com.zoewave.probase.kocolor.features.starterpack.ui.util.BlurHashDecoder
import kotlinx.coroutines.delay

@Composable
fun PackPreviewItemRow(
    item: PackItemDto,
    isSelected: Boolean,
    isTarget: Boolean,
    onToggle: () -> Unit
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
            .background(backgroundColor)
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with subtle rounded square like in mockup
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            val blurhash = item.calculatedBlurhash
            val placeholder = remember(blurhash) {
                BlurHashDecoder.decode(blurhash, 32, 32)?.asImageBitmap()
            }

            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                placeholder = placeholder?.let { BitmapPainter(it) },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(item.colorHex))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.colorHex,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray
                )
            }
        }

        // Using RadioButton logic but style it like the circle in mockup
        RadioButton(
            selected = isSelected,
            onClick = { onToggle() },
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF745E7A).copy(alpha = 0.5f),
                unselectedColor = Color.LightGray
            )
        )
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
                calculatedBlurhash = "LEHV6nWB2yk8pyo0adRj00WBof%M",
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
            onToggle = {}
        )
    }
}
