package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun RecentProductCard(
    uiState: CosmeticItem,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    val onClick = { navTo(KoColorRoute.CosmeticDetail(item.id)) }
    Card(
        modifier = modifier.width(200.dp).height(260.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) {
                AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            
            Surface(modifier = Modifier.padding(16.dp), color = Color.White.copy(alpha = 0.9f), shape = CircleShape) {
                Text(text = item.microCategory.name.uppercase(), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, fontSize = 9.sp)
            }
            
            Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))).padding(16.dp)) {
                Text(text = item.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = item.brand, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview
@Composable
private fun RecentProductCardPreview() {
    MaterialTheme {
        RecentProductCard(
            uiState = CosmeticItem(name = "Sample", brand = "Brand", macroCategory = com.zoewave.probase.core.model.ritual.MacroCategory.COMPLEXION, microCategory = com.zoewave.probase.core.model.ritual.MicroCategory.FOUNDATION),
            onEvent = {},
            navTo = {}
        )
    }
}
