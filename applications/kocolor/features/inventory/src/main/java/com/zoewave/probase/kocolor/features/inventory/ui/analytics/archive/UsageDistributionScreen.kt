package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.UsageDistributionChart
import com.zoewave.probase.kocolor.features.inventory.ui.components.WearRankingRow
import com.zoewave.probase.kocolor.features.inventory.ui.components.WearRankingUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageDistributionScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.applications_kocolor_features_inventory_usage_distribution), 
                        style = MaterialTheme.typography.labelLarge, 
                        letterSpacing = 2.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_usage_metrics_title),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_usage_metrics_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                UsageDistributionChart(
                    items = uiState.items,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_most_worn), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val topUsed = remember(uiState.items) {
                        uiState.items.filter { it.usageCount > 0 }.sortedByDescending { it.usageCount }.take(5)
                    }

                    if (topUsed.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_no_wear_history), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topUsed.forEachIndexed { index, item ->
                                WearRankingRow(
                                    uiState = WearRankingUiState(
                                        item = item,
                                        rank = index + 1,
                                        maxUsage = topUsed.first().usageCount
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Additional insight or description
                Text(
                    text = stringResource(R.string.applications_kocolor_features_inventory_glow_score_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsageDistributionScreenPreview() {
    MaterialTheme {
        UsageDistributionScreen(
            uiState = WardrobeUiState(
                items = listOf(
                    ClothingItem(name = "Blazer", category = ClothingCategory.OUTERWEAR, usageCount = 12, colorHex = "#000000"),
                    ClothingItem(name = "T-Shirt", category = ClothingCategory.TOPS, usageCount = 45, colorHex = "#FFFFFF")
                )
            ),
            navTo = {}
        )
    }
}
