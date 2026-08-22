package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.inventory.ui.components.ProInsightCard
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategicDiversityScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "FOOTPRINT",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF9F9F9)
                )
            )
        },
        containerColor = Color(0xFFF9F9F9),
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "PORTFOLIO COMPOSITION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            val sortedCategories = uiState.categoriesMetadata.toList().sortedByDescending { it.second.itemCount }
            items(sortedCategories) { (name, metadata) ->
                PortfolioCategoryRow(
                    name = name,
                    metadata = metadata,
                    totalItems = uiState.totalItems,
                    totalInvestment = uiState.totalInvestment
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                ProInsightCard(
                    text = if (uiState.totalItems > 0) {
                        "Your wardrobe shows ${uiState.diversityIndex} diversity. " +
                                "Balanced distribution across ${uiState.itemsByCategory.size} verticals."
                    } else {
                        "Start adding items to analyze your strategic diversity."
                    }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PortfolioCategoryRow(
    name: String,
    metadata: CategoryMetadata,
    totalItems: Int,
    totalInvestment: Double
) {
    val percentage = if (totalItems > 0) metadata.itemCount.toFloat() / totalItems else 0f
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val investmentShare = if (totalInvestment > 0) (metadata.totalValue / totalInvestment) * 100 else 0.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${metadata.itemCount} Items | ${currencyFormatter.format(metadata.totalValue)} (${investmentShare.toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StrategicDiversityScreenPreview() {
    MaterialTheme {
        StrategicDiversityScreen(
            uiState = WardrobeUiState(
                totalItems = 54,
                totalInvestment = 6210.0,
                glowScore = 0.0,
                diversityIndex = "Strategic"
            ),
            navTo = {}
        )
    }
}
