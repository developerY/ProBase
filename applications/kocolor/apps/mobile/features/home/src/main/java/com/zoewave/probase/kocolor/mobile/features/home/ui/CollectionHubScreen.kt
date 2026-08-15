package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
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
                // 1. GLOW SYNC HUB - COSMETICS
                item {
                    SyncHubButton(
                        title = "Glow Sync Hub",
                        subtitle = "Cosmetics & Beauty",
                        backgroundColor = Color(0xFF2E1A2C), // Dark Plum
                        shimmerProgress = shimmerProgress,
                        onClick = { navTo(KoColorRoute.StarterPack(filter = "cosmetics")) }
                    )
                }

                // 2. THE VANITY
                item {
                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_vanity_title),
                            count = uiState.totalCosmetics,
                            countLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_items_tracked),
                            valueLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_total_value),
                            value = uiState.totalVanityValue,
                            imageModel = R.drawable.vanity_white_background,
                            icon = Icons.Default.Face,
                            breakdown = uiState.cosmeticsByGroup
                        ),
                        onEvent = { navTo(KoColorRoute.VanityLanding) },
                        navTo = navTo
                    )
                }

                // 3. CLOTHING SYNC HUB
                item {
                    SyncHubButton(
                        title = "Clothing Sync Hub",
                        subtitle = "Apparel & Fashion",
                        backgroundColor = Color(0xFF1A1C2E), // Deep Navy
                        shimmerProgress = shimmerProgress,
                        onClick = { navTo(KoColorRoute.StarterPack(filter = "clothing")) }
                    )
                }

                // 4. THE WARDROBE
                item {
                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_wardrobe_title),
                            count = uiState.totalClothing,
                            countLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_pieces_curated),
                            valueLabel = "TOTAL CLOSET INVESTMENT",
                            value = uiState.totalWardrobeValue,
                            imageModel = R.drawable.wardrobe_background,
                            icon = Icons.Default.Checkroom,
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

data class ArchiveVerticalUiState(
    val title: String,
    val count: Int,
    val countLabel: String,
    val valueLabel: String,
    val value: Double,
    val imageModel: Any,
    val icon: ImageVector,
    val breakdown: Map<String, Int> = emptyMap()
)

@Composable
private fun ArchiveVerticalCard(
    uiState: ArchiveVerticalUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

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
                    Column {
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

                if (uiState.breakdown.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.breakdown.entries.sortedByDescending { it.value }.take(3).forEach { (cat, num) ->
                            Column {
                                Text(
                                    text = num.toString(),
                                    style = MaterialTheme.typography.titleMedium,
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
