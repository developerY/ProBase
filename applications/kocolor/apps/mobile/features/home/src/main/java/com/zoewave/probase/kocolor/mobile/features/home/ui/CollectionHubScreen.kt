package com.zoewave.probase.kocolor.mobile.features.home.ui


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.store.ui.StoreEvent
import com.zoewave.probase.kocolor.features.store.ui.components.BioStoreCard
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.ArchiveVerticalCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.CuratedCollectionCard
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun CollectionHubScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    // --- Shimmer Animation Logic ---
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { LuxuryBrandLogo(uiState = Unit, modifier = Modifier, onEvent = {}, navTo = {}) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_features_home_back))
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_notifications))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Unified Search Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp)
                    .clickable { navTo(KoColorRoute.ColorSearch) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_search_all),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { navTo(KoColorRoute.ColorSearch) }) {
                        Icon(Icons.Default.Palette, null, tint = Color.Gray)
                    }
                    IconButton(onClick = { navTo(KoColorRoute.Camera("color_scan")) }) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                // 1. THE VANITY (with incorporated Discover Cosmetics pill, PAO hygiene, restock & chromatic DNA)
                item {
                    val cosmeticsProgress = remember(uiState.cosmeticsByGroup) {
                        val comp = uiState.cosmeticsByGroup.entries.filter { it.key.contains("COMPLEXION", ignoreCase = true) || it.key.contains("DIMENSION", ignoreCase = true) }.sumOf { it.value }
                        val eyes = uiState.cosmeticsByGroup.entries.filter { it.key.contains("EYE", ignoreCase = true) || it.key.startsWith("TOOL", ignoreCase = true) }.sumOf { it.value }
                        val nails = uiState.cosmeticsByGroup.entries.filter { it.key.contains("NAIL", ignoreCase = true) }.sumOf { it.value }
                        val lips = uiState.cosmeticsByGroup.entries.filter { it.key.contains("LIP", ignoreCase = true) }.sumOf { it.value }
                        val prep = uiState.cosmeticsByGroup.entries.filter { it.key.contains("PREP", ignoreCase = true) || it.key.contains("SKIN", ignoreCase = true) }.sumOf { it.value }
                        val hair = uiState.cosmeticsByGroup.entries.filter { it.key.contains("HAIR", ignoreCase = true) }.sumOf { it.value }
                        val oral = uiState.cosmeticsByGroup.entries.filter { it.key.contains("ORAL", ignoreCase = true) }.sumOf { it.value }
                        val body = uiState.cosmeticsByGroup.entries.filter { it.key.startsWith("HYGIENE", ignoreCase = true) || it.key.contains("FRAGRANCE", ignoreCase = true) || it.key.contains("GROOMING", ignoreCase = true) }.sumOf { it.value }

                        listOf(
                            CategoryProgressItem("COMP", comp, 27, Color(0xFFD4AF37)),
                            CategoryProgressItem("EYES", eyes, 52, Color(0xFF4A2B4B)),
                            CategoryProgressItem("NAILS", nails, 10, Color(0xFF8B263E)),
                            CategoryProgressItem("LIPS", lips, 16, Color(0xFFC25975)),
                            CategoryProgressItem("PREP", prep, 14, Color(0xFF3B5249)),
                            CategoryProgressItem("HAIR", hair, 13, Color(0xFF415A77)),
                            CategoryProgressItem("ORAL", oral, 8, Color(0xFF5C6B73)),
                            CategoryProgressItem("BODY", body, 33, Color(0xFF8D5B4C))
                        )
                    }

                    val expiringCount = uiState.expiringCosmeticsCount
                    val healthText = if (expiringCount > 0) "$expiringCount items expiring this month (PAO Alert)" else if (uiState.totalCosmetics > 0) "Vanity Health: 98% Fresh" else null
                    val restockText = if (uiState.totalCosmetics > 0) "Restock needed: 2 formulas low" else null
                    val toneText = if (uiState.totalCosmetics > 0) "Signature Tone: Roseate Sand · Warm/Matte" else null

                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = "Vanity",
                            count = uiState.totalCosmetics,
                            countLabel = "items",
                            valueLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_total_value),
                            value = uiState.totalVanityValue,
                            imageModel = R.drawable.vanity_white_background,
                            icon = Icons.Default.Face,
                            discoverTitle = "DISCOVER",
                            discoverSubtitle = "Cosmetics Catalog",
                            discoverColor = Color(0xFF3D223B), // Dark Plum
                            onDiscoverClick = { navTo(KoColorRoute.StarterPack(filter = "cosmetics", showHero = true)) },
                            categoryProgress = cosmeticsProgress,
                            healthMetric = healthText,
                            restockMetric = restockText,
                            chromaticTone = toneText,
                            avgCpu = if (uiState.totalCosmetics > 0) 1.20 else null,
                            breakdown = uiState.cosmeticsByGroup
                        ),
                        onEvent = { navTo(KoColorRoute.VanityLanding) },
                        navTo = navTo
                    )
                }

                // 2. THE WARDROBE (with incorporated Explore Fashion pill, CPW & rotation health)
                item {
                    val wardrobeProgress = remember(uiState.clothingByCategory) {
                        val tops = uiState.clothingByCategory.entries.filter { it.key.contains("TOP", ignoreCase = true) || it.key.contains("SHIRT", ignoreCase = true) }.sumOf { it.value }
                        val bots = uiState.clothingByCategory.entries.filter { it.key.contains("BOT", ignoreCase = true) || it.key.contains("PANT", ignoreCase = true) }.sumOf { it.value }
                        val dress = uiState.clothingByCategory.entries.filter { it.key.contains("DRESS", ignoreCase = true) }.sumOf { it.value }
                        val outer = uiState.clothingByCategory.entries.filter { it.key.contains("OUTER", ignoreCase = true) || it.key.contains("JACKET", ignoreCase = true) || it.key.contains("COAT", ignoreCase = true) }.sumOf { it.value }
                        val active = uiState.clothingByCategory.entries.filter { it.key.contains("ACTIVE", ignoreCase = true) }.sumOf { it.value }
                        val shoes = uiState.clothingByCategory.entries.filter { it.key.contains("SHOE", ignoreCase = true) }.sumOf { it.value }

                        listOf(
                            CategoryProgressItem("TOPS", tops, 9, Color(0xFF3B5249)),
                            CategoryProgressItem("BOTS", bots, 9, Color(0xFF415A77)),
                            CategoryProgressItem("DRESS", dress, 9, Color(0xFF8B263E)),
                            CategoryProgressItem("OUTER", outer, 9, Color(0xFF8D5B4C)),
                            CategoryProgressItem("ACTIVE", active, 9, Color(0xFFD4AF37)),
                            CategoryProgressItem("SHOES", shoes, 9, Color(0xFF774936))
                        )
                    }

                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = "Wardrobe",
                            count = uiState.totalClothing,
                            countLabel = "pieces",
                            valueLabel = "TOTAL CLOSET INVESTMENT",
                            value = uiState.totalWardrobeValue,
                            imageModel = R.drawable.wardrobe_background,
                            icon = Icons.Default.Checkroom,
                            discoverTitle = "EXPLORE",
                            discoverSubtitle = "Fashion Catalog",
                            discoverColor = Color(0xFF1B2238), // Deep Navy
                            onDiscoverClick = { navTo(KoColorRoute.StarterPack(filter = "clothing", showHero = true)) },
                            categoryProgress = wardrobeProgress,
                            healthMetric = if (uiState.totalClothing > 0) "Rotation Health: 94% Active" else null,
                            chromaticTone = if (uiState.totalClothing > 0) "Palette Baseline: Neutral-led · Warm-biased" else null,
                            avgCpu = if (uiState.totalClothing > 0) 4.20 else null,
                            breakdown = uiState.clothingByCategory
                        ),
                        onEvent = { navTo(KoColorRoute.WardrobeLanding) },
                        navTo = navTo
                    )
                }

                // The Store
                //item { Spacer(modifier = Modifier.height(48.dp)) }
                item {
                    BioStoreCard(
                        uiState = uiState.storeUiState,
                        onEvent = { event ->
                            android.util.Log.d("HomeScreen", "StoreEvent received: $event")
                            when (event) {
                                StoreEvent.ToggleExpansion -> onEvent(HomeEvent.ToggleStoreExpansion)
                                StoreEvent.EnterStore -> {
                                    android.util.Log.d("HomeScreen", "Routing to Store")
                                    navTo(KoColorRoute.Store)
                                }
                                else -> {}
                            }
                        },
                        navTo = navTo,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                if (uiState.savedSuggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "BLUEPRINT HISTORY",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(uiState.savedSuggestions) { analysis ->
                        CuratedCollectionCard(
                            uiState = CuratedCollectionUiState(analysis),
                            onEvent = { navTo(KoColorRoute.CollectionDetail(analysis.id)) },
                            navTo = navTo
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CollectionHubScreenPreview() {
    com.zoewave.probase.kocolor.mobile.core.ui.theme.KoColorTheme {
        CollectionHubScreen(
            uiState = HomeUiState(
                totalCosmetics = 120,
                totalClothing = 45,
                totalVanityValue = 1250.0,
                totalWardrobeValue = 3400.0,
                expiringCosmeticsCount = 3,
                cosmeticsByGroup = mapOf(
                    "COMPLEXION" to 30,
                    "EYES" to 40,
                    "LIPS" to 20,
                    "SKIN PREP" to 30
                ),
                clothingByCategory = mapOf(
                    "TOPS" to 20,
                    "BOTTOMS" to 15,
                    "OUTERWEAR" to 5,
                    "SHOES" to 5
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

