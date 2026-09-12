package com.zoewave.probase.kocolor.features.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.R

@Composable
fun UsageDistributionChart(
    items: List<ClothingItem>,
    modifier: Modifier = Modifier
) {
    val distribution = remember(items) {
        val counts = IntArray(5)
        items.forEach { item ->
            when {
                item.usageCount == 0 -> counts[0]++
                item.usageCount in 1..5 -> counts[1]++
                item.usageCount in 6..10 -> counts[2]++
                item.usageCount in 11..20 -> counts[3]++
                else -> counts[4]++
            }
        }
        counts.toList()
    }

    val maxCount = distribution.maxOrNull() ?: 1
    val labels = listOf(
        stringResource(R.string.applications_kocolor_features_inventory_never_worn),
        stringResource(R.string.applications_kocolor_features_inventory_rarely_worn),
        stringResource(R.string.applications_kocolor_features_inventory_occasionally_worn),
        stringResource(R.string.applications_kocolor_features_inventory_regularly_worn),
        stringResource(R.string.applications_kocolor_features_inventory_wardrobe_heroes)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_inventory_usage_distribution),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            distribution.forEachIndexed { index, count ->
                val ratio = if (maxCount > 0) count.toFloat() / maxCount else 0f
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (count > 0) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio.coerceAtLeast(0.05f))
                            .background(
                                color = if (index == 4) Color(0xFFD4AF37) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsageDistributionChartPreview() {
    MaterialTheme {
        UsageDistributionChart(
            items = listOf(
                ClothingItem(name = "Item 1", category = ClothingCategory.TOPS, usageCount = 0, colorHex = "#FFFFFF"),
                ClothingItem(name = "Item 2", category = ClothingCategory.TOPS, usageCount = 3, colorHex = "#FFFFFF"),
                ClothingItem(name = "Item 3", category = ClothingCategory.TOPS, usageCount = 8, colorHex = "#FFFFFF"),
                ClothingItem(name = "Item 4", category = ClothingCategory.TOPS, usageCount = 15, colorHex = "#FFFFFF"),
                ClothingItem(name = "Item 5", category = ClothingCategory.TOPS, usageCount = 25, colorHex = "#FFFFFF"),
                ClothingItem(name = "Item 6", category = ClothingCategory.TOPS, usageCount = 30, colorHex = "#FFFFFF")
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
