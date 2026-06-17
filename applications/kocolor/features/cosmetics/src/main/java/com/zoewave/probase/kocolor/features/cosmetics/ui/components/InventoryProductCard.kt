package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.*

@Composable
fun InventoryProductCard(
    uiState: CosmeticItem,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEvent() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.LightGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.macroCategory.displayName.uppercase()} • ${item.batchCode ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2420),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StockStatusBadge(item.fillLevel ?: 0.0)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FooterDetail(item)
                    
                    Text(
                        text = item.price?.let { 
                            NumberFormat.getCurrencyInstance(Locale.US).format(it)
                        } ?: "$0.00",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2420)
                    )
                }
            }
        }
    }
}

@Composable
private fun StockStatusBadge(fillLevel: Double) {
    val (text, color) = when {
        fillLevel > 0.5 -> stringResource(R.string.applications_kocolor_features_cosmetics_in_stock) to Color(0xFF81C784)
        fillLevel > 0.1 -> stringResource(R.string.applications_kocolor_features_cosmetics_low_stock) to Color(0xFFE57373)
        else -> "Out of Stock" to Color(0xFFBDBDBD)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FooterDetail(item: CosmeticItem) {
    val colorHex = item.colorHex
    if (colorHex != null || item.shadeName != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Shade: ${item.shadeName ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (colorHex != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                        .border(0.5.dp, Color.LightGray, CircleShape)
                )
            }
        }
    } else {
        Text(
            text = "Size: ${item.volume ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryProductCardPreview() {
    MaterialTheme {
        InventoryProductCard(
            uiState = CosmeticItem(name = "Sample Product", brand = "Brand", macroCategory = com.zoewave.probase.core.model.ritual.MacroCategory.COMPLEXION, microCategory = com.zoewave.probase.core.model.ritual.MicroCategory.FOUNDATION),
            onEvent = {},
            navTo = {}
        )
    }
}
