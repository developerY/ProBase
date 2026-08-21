package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MonetizationOn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.StatIcon
import com.zoewave.probase.kocolor.features.inventory.ui.components.SummaryStatCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.SummaryStatUiState
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
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.applications_kocolor_features_inventory_strategic_diversity_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatIcon(
                        icon = Icons.Default.AutoAwesome,
                        value = "${(uiState.glowScore * 100).toInt()}%",
                        label = stringResource(R.string.applications_kocolor_features_inventory_glow_score_label),
                        onClick = { navTo(KoColorRoute.UsageDistribution) },
                        modifier = Modifier.weight(1f)
                    )

                    StatIcon(
                        icon = Icons.Default.Explore,
                        value = uiState.diversityIndex,
                        label = stringResource(R.string.applications_kocolor_features_inventory_diversity_label),
                        onClick = null, // Already on this screen
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatCard(
                        uiState = SummaryStatUiState(
                            label = stringResource(R.string.applications_kocolor_features_inventory_total_pieces_label),
                            value = uiState.totalItems.toString(),
                            icon = Icons.Default.Checkroom
                        ),
                        modifier = Modifier.weight(1f),
                        onEvent = { navTo(KoColorRoute.WardrobeAnalytics) },
                        navTo = navTo
                    )

                    SummaryStatCard(
                        uiState = SummaryStatUiState(
                            label = stringResource(R.string.applications_kocolor_features_inventory_total_value_label),
                            value = currencyFormatter.format(uiState.totalInvestment),
                            icon = Icons.Default.MonetizationOn
                        ),
                        modifier = Modifier.weight(1f),
                        onEvent = { navTo(KoColorRoute.Wardrobe) },
                        navTo = navTo
                    )
                }
            }
        }
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
