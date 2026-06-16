package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.*
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory

@Preview(showBackground = true)
@Composable
private fun VanityLandingScreenPreview() {
    MaterialTheme {
        VanityLandingScreen(
            uiState = CosmeticsUiState(
                totalCosmetics = 34,
                items = listOf(CosmeticItem(name = "Sample", brand = "Brand", macroCategory = MacroCategory.COMPLEXION, microCategory = MicroCategory.FOUNDATION))
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VanityLandingScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    if (showTaxonomyInfo) {
        ProfessionalTaxonomyDialog(onDismiss = { showTaxonomyInfo = false })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_glow_archive), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.BoxCapture()) }) { 
                        Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add_item)) 
                    }
                    IconButton(onClick = { navTo(KoColorRoute.InventoryManagement) }) { Icon(Icons.Default.Inventory2, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_inventory_title)) }
                    IconButton(onClick = { navTo(KoColorRoute.ColorSearch) }) { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_filter)) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.CosmeticAdd()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add_item))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Welcome Header
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_welcome_header),
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_welcome_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryStatCard(
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_total_products),
                        value = uiState.totalCosmetics.toString(),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f),
                        onClick = { navTo(KoColorRoute.CosmeticAnalytics) }
                    )
                    SummaryStatCard(
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_expiring_soon),
                        value = uiState.expiringCosmeticsCount.toString(),
                        icon = Icons.Default.ErrorOutline,
                        modifier = Modifier.weight(1f),
                        onClick = { navTo(KoColorRoute.ExpiringSoon) }
                    )
                }
            }

            // 3. Category Hero Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_categories),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            onClick = { showTaxonomyInfo = true },
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFD4AF37)), // Golden Border
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "i",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF2C2420)
                                )
                            }
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val sections = listOf(
                            "Skincare & Prep" to (Color(0xFFF7F2EB) to R.drawable.vanity_skincare),
                            "Complexion" to (Color(0xFFF9F6F0) to R.drawable.vanity_complexion),
                            "Color & Dimension" to (Color(0xFFFDEEF4) to R.drawable.vanity_color),
                            "Eyes & Brows" to (Color(0xFFE8F1FD) to R.drawable.vanity_eyes),
                            "Lips" to (Color(0xFFFEECEB) to R.drawable.vanity_lips)
                        )
                        
                        sections.forEach { (name, props) ->
                            val (bgColor, fallbackImage) = props
                            val metadata = uiState.categoriesMetadata.entries.find { it.key.contains(name, ignoreCase = true) }?.value
                            VanityCategoryCard(
                                name = name,
                                metadata = metadata,
                                baseColor = bgColor,
                                fallbackImage = fallbackImage,
                                navTo = navTo,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 4. Recently Added
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_recently_added), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(
                            stringResource(R.string.applications_kocolor_features_cosmetics_see_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navTo(KoColorRoute.Cosmetics()) }
                        )
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.items.take(5)) { item ->
                            RecentProductCard(
                                uiState = item,
                                navTo = navTo
                            )
                        }
                    }
                }
            }
        }
    }
}
