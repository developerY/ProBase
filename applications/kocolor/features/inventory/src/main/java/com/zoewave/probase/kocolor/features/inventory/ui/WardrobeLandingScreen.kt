package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.AtelierWardrobeCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.AtelierWardrobeUiState
import com.zoewave.probase.kocolor.features.inventory.ui.components.RecentClothingCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeTaxonomyDialog
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun WardrobeLandingScreenPreview() {
    MaterialTheme {
        WardrobeLandingScreen(
            uiState = WardrobeUiState(
                totalItems = 9,
                totalInvestment = 1615.0,
                items = listOf(
                    ClothingItem(internalId = 1, name = "Blouse", category = ClothingCategory.TOPS, colorHex = "#FFFFFF")
                ),
                glowScore = 0.84,
                diversityIndex = "Eclectic"
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeLandingScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.applications_kocolor_features_inventory_style_archive_title),
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
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Wardrobe) }) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = stringResource(R.string.applications_kocolor_features_inventory_inventory)
                        )
                    }
                    IconButton(onClick = { navTo(KoColorRoute.ColorSearch) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.applications_kocolor_features_inventory_search)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.StarterPack(filter = "clothing")) },
                containerColor = Color(0xFF5A3854), // Dark Plum matching image
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Discover Fashion")
            }
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 100.dp,
                start = 16.dp,
                end = 16.dp,
                top = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CuratedClosetDashboard(
                    totalPieces = uiState.totalItems,
                    totalValue = uiState.totalInvestment,
                    glowScore = uiState.glowScore.toFloat(),
                    diversityLabel = uiState.diversityIndex,
                    onViewIntelligenceClicked = { navTo(KoColorRoute.WardrobeAnalytics) },
                    onViewInventoryClicked = { navTo(KoColorRoute.Wardrobe) },
                    onViewFootprintClicked = { navTo(KoColorRoute.StrategicDiversity) },
                    onViewBehaviorClicked = { navTo(KoColorRoute.UsageDistribution) }
                )
            }

            item {
                var showTaxonomyInfo by remember { mutableStateOf(false) }
                if (showTaxonomyInfo) {
                    WardrobeTaxonomyDialog(
                        uiState = Unit,
                        onEvent = { showTaxonomyInfo = false },
                        navTo = {}
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_inventory_verticals_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            onClick = { showTaxonomyInfo = true },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD4AF37)),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_inventory_info_icon),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF2C2420)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val sections = listOf(
                            "Tops" to (Color(0xFFF7F2EB) to R.drawable.tops),
                            "Bottoms" to (Color(0xFFF9F6F0) to R.drawable.bottom),
                            "Outerwear" to (Color(0xFFF1F4F9) to R.drawable.wardrobe_tops),
                            "Activewear" to (Color(0xFFFDEEF4) to R.drawable.wardrobe_accessories),
                            "Dresses" to (Color(0xFFF5F2F8) to R.drawable.tops),
                            "Shoes" to (Color(0xFFE8F1FD) to R.drawable.wardrobe_shoes),
                            "Accessories" to (Color(0xFFF3EBFD) to R.drawable.wardrobe_accessories)
                        )

                        sections.forEach { (name, props) ->
                            val (bgColor, imageModel) = props
                            val metadata = uiState.categoriesMetadata.entries.find {
                                it.key.equals(
                                    name,
                                    ignoreCase = true
                                )
                            }?.value
                            AtelierWardrobeCard(
                                uiState = AtelierWardrobeUiState(
                                    name = name,
                                    metadata = metadata,
                                    baseColor = bgColor,
                                    imageModel = imageModel
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                onEvent = {},
                                navTo = navTo
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.applications_kocolor_features_inventory_recently_added),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            stringResource(R.string.applications_kocolor_features_inventory_see_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navTo(KoColorRoute.Wardrobe) }
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.items.take(5)) { item ->
                            RecentClothingCard(
                                uiState = item,
                                modifier = Modifier,
                                onEvent = {},
                                navTo = navTo
                            )
                        }
                    }
                }
            }
        }
    }
}