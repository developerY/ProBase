package com.zoewave.probase.kocolor.features.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem

@Composable
fun WearRankingRow(item: ClothingItem, rank: Int, maxUsage: Int) {
    val progress = item.usageCount.toFloat() / maxUsage.coerceAtLeast(1)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = rank.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(
                        R.string.applications_kocolor_features_inventory_brand_wears_format, 
                        item.brand ?: stringResource(R.string.applications_kocolor_features_inventory_archive_label), 
                        item.usageCount
                    ), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(item.colorHex?.let { parseColor(it) } ?: item.dominantHex?.let { parseColor(it) } ?: Color.Gray)
                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WearRankingRowPreview() {
    MaterialTheme {
        WearRankingRow(
            item = ClothingItem(name = "Blazer", category = ClothingCategory.TOPS, usageCount = 12),
            rank = 1,
            maxUsage = 100
        )
    }
}
