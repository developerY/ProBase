package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.store.ui.StoreEvent
import com.zoewave.probase.kocolor.features.store.ui.components.BioStoreCard
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.CategoryProgressSection
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                            title = "Wear",
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

@Composable
private fun SyncHubButton(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    shimmerProgress: Float,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp), // Slightly shorter for 4-button layout
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        shadowElevation = 6.dp
    ) {
        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.05f),
                Color(0xFFD4AF37).copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.05f),
                Color.Transparent
            ),
            start = Offset(x = shimmerProgress * 1200f, y = 0f),
            end = Offset(x = (shimmerProgress + 0.4f) * 1200f, y = 600f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .background(shimmerBrush)
                .padding(horizontal = 24.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }

            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun CuratedCollectionCard(
    uiState: CuratedCollectionUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault()) }
    val dateStr = dateFormat.format(java.util.Date(analysis.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEvent() },
        shape = RoundedCornerShape(24.dp), // More rounded as per image
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray.copy(alpha = 0.6f)
                )

                // Seasonal Badge
                Surface(
                    color = Color(0xFFF3E5F5), // Light Lavender
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = analysis.advice.seasonalType.name.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF745E7A),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = analysis.advice.title ?: "The Personal Collection",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Local Architect: ${analysis.advice.summary}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                analysis.advice.recommendedPalette.take(4).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
                    )
                }
            }
        }
    }
}



@Composable
private fun ArchiveVerticalCard(
    uiState: ArchiveVerticalUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onEvent() },
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF3ECEF)
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            AsyncImage(
                model = uiState.imageModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize().alpha(0.2f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Translucent frosted glass pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp),
                        shape = RoundedCornerShape(44.dp),
                        color = Color.White.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.title,
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (uiState.onDiscoverClick != null) {
                        // Floating Discover Badge (Intersecting top-right of pill)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (-12).dp)
                                .size(52.dp),
                            shape = CircleShape,
                            color = Color(0xFFEFE8E1).copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, Color.White),
                            shadowElevation = 4.dp,
                            onClick = uiState.onDiscoverClick
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(44.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = uiState.icon,
                                            contentDescription = uiState.discoverTitle ?: "Discover",
                                            tint = Color(0xFF1C1B1F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    // Small gold '+' add button badge overlapping the bottom right corner
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFD700), // Gold
                                        border = BorderStroke(1.5.dp, Color(0xFFEFE8E1).copy(alpha = 0.95f)),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTTOM HALF: Items Tracked Row with Chevron (overlapping pill bottom edge)
                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ChevronRotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .offset(y = (-14).dp)
                        .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.count} ${uiState.countLabel}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937),
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF8C7F72),
                        modifier = Modifier.size(20.dp).rotate(chevronRotation)
                    )
                }

                // Collapsible Content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TOTAL VALUE + AVG CPU / Health Metric
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = uiState.valueLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = currencyFormatter.format(uiState.value),
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            }

                            uiState.avgCpu?.let { cpu ->
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2E7D32))
                                        )
                                        Text(
                                            text = "AVG CPU: ${currencyFormatter.format(cpu)} / use",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        CategoryProgressSection(categoryProgress = uiState.categoryProgress)

                        if (uiState.chromaticTone != null || uiState.healthMetric != null || uiState.restockMetric != null) {
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.chromaticTone?.let { tone ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF8E24AA), modifier = Modifier.size(14.dp))
                                        Row {
                                            Text(
                                                text = "Signature Tone: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = tone.removePrefix("Signature Tone: "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                uiState.healthMetric?.let { health ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(14.dp))
                                        Row {
                                            Text(
                                                text = "Vanity Health: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828),
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = health.removePrefix("Vanity Health: ").removeSuffix(" (PAO Alert)"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                uiState.restockMetric?.let { restock ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFB8C00), modifier = Modifier.size(14.dp))
                                        Row {
                                            Text(
                                                text = "Restock needed: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100),
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = restock.removePrefix("Restock needed: "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CollectionHubScreenPreview() {
    MaterialTheme {
        CollectionHubScreen(
            uiState = HomeUiState(
                totalCosmetics = 117,
                totalClothing = 54,
                totalVanityValue = 7577.00,
                totalWardrobeValue = 6210.00,
                cosmeticsByGroup = mapOf(
                    "Complexion" to 58,
                    "Eyes & Brows" to 20,
                    "Nails" to 18
                ),
                clothingByCategory = mapOf(
                    "Tops" to 16,
                    "Bottoms" to 9,
                    "Shoes" to 7
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Vanity Card with Discover Pill")
@Composable
private fun VanityCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ArchiveVerticalCard(
                uiState = ArchiveVerticalUiState(
                    title = "The Vanity",
                    count = 117,
                    countLabel = "items curated",
                    valueLabel = "TOTAL VALUE",
                    value = 7577.00,
                    imageModel = R.drawable.vanity_white_background,
                    icon = Icons.Default.Face,
                    discoverTitle = "DISCOVER",
                    discoverSubtitle = "Cosmetics",
                    discoverColor = Color(0xFF3D223B),
                    onDiscoverClick = {},
                    breakdown = mapOf(
                        "Complexion" to 58,
                        "Eyes & Brows" to 20,
                        "Nails" to 18
                    )
                ),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
