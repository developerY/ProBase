package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import kotlinx.coroutines.delay

@Composable
fun PackPreviewItemRow(
    item: PackItem,
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
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
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
            val subtitleText = remember(item.brand, item.shade) {
                if (!item.shade.isNullOrBlank()) {
                    "${item.brand} • ${item.shade}"
                } else {
                    item.brand
                }
            }
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(item.hexColor))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.hexColor,
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
            item = PackItem(
                id = "1",
                name = "KoColor Purifying Gel Cleanser",
                brand = "KoColor",
                shade = "Clear Crystal",
                hexColor = "#F4F6F0",
                thumbnailUrl = "",
                imageUrl = ""
            ),
            isSelected = true,
            isTarget = false,
            onToggle = {}
        )
    }
}
