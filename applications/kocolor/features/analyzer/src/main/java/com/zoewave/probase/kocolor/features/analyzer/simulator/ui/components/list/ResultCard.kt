package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun ResultCard(
    clothingItem: ClothingItem? = null,
    cosmeticItem: CosmeticItem? = null,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val name = clothingItem?.name ?: cosmeticItem?.name ?: ""
    val brand = clothingItem?.brand ?: cosmeticItem?.brand ?: stringResource(R.string.applications_kocolor_features_analyzer_simulator_bespoke)
    val category = clothingItem?.category?.name ?: cosmeticItem?.microCategory?.displayName ?: ""
    val imageUrl = clothingItem?.imageUrl ?: cosmeticItem?.imageUrl
    val colorHex = clothingItem?.colorHex ?: cosmeticItem?.colorHex

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.fillMaxHeight().width(120.dp).background(colorHex?.let { parseColor(it) } ?: Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color.Black.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = category, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                Text(text = brand, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
