package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.rememberBlurHashPainter
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticsEvent
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun CosmeticProductCard(
    uiState: CosmeticItem,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val contentColor = if (uiState.colorHex != null && isColorDark(cardColor)) Color.White else Color.Black

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navTo(KoColorRoute.CosmeticDetail(uiState.internalId)) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor, contentColor = contentColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product Image or Swatch
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(contentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUrl != null) {
                        val placeholder = rememberBlurHashPainter(
                            blurHash = uiState.blurhash,
                            fallbackColor = cardColor.copy(alpha = 0.2f)
                        )
                        AsyncImage(
                            model = uiState.imageUrl, 
                            contentDescription = null, 
                            placeholder = placeholder,
                            modifier = Modifier.fillMaxSize(), 
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, tint = contentColor.copy(alpha = 0.5f))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = uiState.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = uiState.brand, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
                }
                
                IconButton(onClick = { onEvent(CosmeticsEvent.DeleteItem(uiState.internalId)) }) {
                    Icon(Icons.Default.Delete, null, tint = contentColor.copy(alpha = 0.6f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_cost_per_use_label), style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                    Text(text = uiState.costPerUse?.let { "$%.2f".format(it) } ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onEvent(CosmeticsEvent.UseItem(uiState.internalId)) },
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor.copy(alpha = 0.2f), contentColor = contentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_use_action), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
