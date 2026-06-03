package com.zoewave.probase.kocolor.features.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun ClothingProductGridCard(uiState: ClothingItem, navTo: (KoColorRoute) -> Unit) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.WardrobeDetail(item.id)) }
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.75f).clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: Color.White
                
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(20.dp)
                        .align(Alignment.TopEnd),
                    color = itemColor,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {}
            } else {
                val itemColor = item.dominantHex?.let { parseColor(it) } 
                    ?: item.colorHex?.let { parseColor(it) } 
                    ?: MaterialTheme.colorScheme.surfaceVariant
                Box(modifier = Modifier.fillMaxSize().background(itemColor))
            }

            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), startY = 200f)))
            
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(text = item.brand ?: "", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Black)
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}
