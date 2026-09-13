package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.mobile.features.home.R
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
                // 1. THE VANITY (with incorporated Discover Cosmetics pill)
                item {
                    val cosmeticsProgress = remember(uiState.cosmeticsByGroup) {
                        val comp = uiState.cosmeticsByGroup["COMPLEXION"] ?: 0
                        val eyes = uiState.cosmeticsByGroup["EYES & BROWS"] ?: 0
                        val nails = uiState.cosmeticsByGroup["NAILS"] ?: 0
                        val lips = uiState.cosmeticsByGroup["LIPS"] ?: 0

                        listOf(
                            CategoryProgressItem("COMP", comp, if (comp > 0) (comp * 1.4f).toInt().coerceAtLeast(20) else 20, Color(0xFFD4AF37)),
                            CategoryProgressItem("EYES", eyes, if (eyes > 0) (eyes * 1.5f).toInt().coerceAtLeast(15) else 15, Color(0xFF4A2B4B)),
                            CategoryProgressItem("NAILS", nails, if (nails > 0) (nails * 1.5f).toInt().coerceAtLeast(10) else 10, Color(0xFF8B263E)),
                            CategoryProgressItem("LIPS", lips, if (lips > 0) (lips * 1.5f).toInt().coerceAtLeast(15) else 15, Color(0xFFC25975))
                        )
                    }

                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_vanity_title),
                            count = uiState.totalCosmetics,
                            countLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_items_tracked),
                            valueLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_total_value),
                            value = uiState.totalVanityValue,
                            imageModel = R.drawable.vanity_white_background,
                            icon = Icons.Default.Face,
                            discoverTitle = "DISCOVER",
                            discoverSubtitle = "Cosmetics",
                            discoverColor = Color(0xFF3D223B), // Dark Plum
                            onDiscoverClick = { navTo(KoColorRoute.StarterPack(filter = "cosmetics")) },
                            categoryProgress = cosmeticsProgress,
                            breakdown = uiState.cosmeticsByGroup
                        ),
                        onEvent = { navTo(KoColorRoute.VanityLanding) },
                        navTo = navTo
                    )
                }

                // 2. THE WARDROBE (with incorporated Explore Fashion pill)
                item {
                    val wardrobeProgress = remember(uiState.clothingByCategory) {
                        val tops = uiState.clothingByCategory["TOPS"] ?: 0
                        val bots = uiState.clothingByCategory["BOTTOMS"] ?: 0
                        val shoes = uiState.clothingByCategory["SHOES"] ?: 0
                        val outer = uiState.clothingByCategory["OUTERWEAR"] ?: 0

                        listOf(
                            CategoryProgressItem("TOPS", tops, if (tops > 0) (tops * 1.5f).toInt().coerceAtLeast(15) else 15, Color(0xFF3B5249)),
                            CategoryProgressItem("BOTS", bots, if (bots > 0) (bots * 1.5f).toInt().coerceAtLeast(10) else 10, Color(0xFF415A77)),
                            CategoryProgressItem("SHOES", shoes, if (shoes > 0) (shoes * 1.5f).toInt().coerceAtLeast(8) else 8, Color(0xFF774936)),
                            CategoryProgressItem("OUTER", outer, if (outer > 0) (outer * 1.5f).toInt().coerceAtLeast(6) else 6, Color(0xFF8D5B4C))
                        )
                    }

                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_wardrobe_title),
                            count = uiState.totalClothing,
                            countLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_pieces_curated),
                            valueLabel = "TOTAL CLOSET INVESTMENT",
                            value = uiState.totalWardrobeValue,
                            imageModel = R.drawable.wardrobe_background,
                            icon = Icons.Default.Checkroom,
                            discoverTitle = "EXPLORE",
                            discoverSubtitle = "Fashion",
                            discoverColor = Color(0xFF1B2238), // Deep Navy
                            onDiscoverClick = { navTo(KoColorRoute.StarterPack(filter = "clothing")) },
                            categoryProgress = wardrobeProgress,
                            breakdown = uiState.clothingByCategory
                        ),
                        onEvent = { navTo(KoColorRoute.WardrobeLanding) },
                        navTo = navTo
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

data class CuratedCollectionUiState(val analysis: SavedAnalysis)

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

data class CategoryProgressItem(
    val label: String,
    val owned: Int,
    val target: Int,
    val color: Color
)

data class ArchiveVerticalUiState(
    val title: String,
    val count: Int,
    val countLabel: String,
    val valueLabel: String,
    val value: Double,
    val imageModel: Any,
    val icon: ImageVector,
    val discoverTitle: String? = null,
    val discoverSubtitle: String? = null,
    val discoverColor: Color = Color(0xFF3D223B),
    val onDiscoverClick: (() -> Unit)? = null,
    val categoryProgress: List<CategoryProgressItem> = emptyList(),
    val breakdown: Map<String, Int> = emptyMap()
)


@Composable
private fun TopRightVanityActionCard(
    discoverTitle: String,
    discoverSubtitle: String,
    backgroundColor: Color,
    icon: ImageVector,
    categoryProgress: List<CategoryProgressItem>,
    onDiscoverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation"
    )

    Surface(
        modifier = modifier.width(180.dp),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Header: Dark Pill Button + Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDiscoverClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = discoverTitle.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = discoverSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.rotate(chevronRotation)
                    )
                }
            }

            // Divider Line
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.15f),
                thickness = 1.dp
            )

            // Collapsible / Vertically Scrollable Details Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoryProgress.forEach { progress ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(progress.color)
                                    )
                                    Text(
                                        text = progress.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 8.sp,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${progress.owned}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "/${progress.target}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 8.sp
                                    )
                                }
                            }

                            // Progress Bar
                            val fillRatio = if (progress.target > 0) (progress.owned.toFloat() / progress.target.toFloat()).coerceIn(0f, 1f) else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = fillRatio)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(progress.color)
                                )
                            }
                        }
                    }
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onEvent() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = uiState.imageModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize().alpha(0.15f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "${uiState.count} ${uiState.countLabel}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }

                    if (uiState.discoverTitle != null && uiState.onDiscoverClick != null) {
                        TopRightVanityActionCard(
                            discoverTitle = uiState.discoverTitle,
                            discoverSubtitle = uiState.discoverSubtitle ?: "Cosmetics Catalog",
                            backgroundColor = uiState.discoverColor,
                            icon = uiState.icon,
                            categoryProgress = uiState.categoryProgress,
                            onDiscoverClick = uiState.onDiscoverClick
                        )
                    } else {
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(uiState.icon, null, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                if (uiState.categoryProgress.isEmpty() && uiState.breakdown.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.breakdown.entries.sortedByDescending { it.value }.take(3).forEach { (cat, num) ->
                            Column {
                                Text(
                                    text = num.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = cat.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = currencyFormatter.format(uiState.value),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = uiState.valueLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.Gray
                    )
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
