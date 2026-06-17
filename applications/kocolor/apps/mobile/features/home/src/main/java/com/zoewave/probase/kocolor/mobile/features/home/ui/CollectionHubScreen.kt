package com.zoewave.probase.kocolor.mobile.features.home.ui

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.components.LuxuryBrandLogo
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionHubScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { LuxuryBrandLogo(uiState = Unit, onEvent = {}, navTo = {}) },
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
        }
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
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

                item {
                    ArchiveVerticalCard(
                        uiState = ArchiveVerticalUiState(
                            title = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_wardrobe_title),
                            count = uiState.totalClothing,
                            countLabel = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_pieces_curated),
                            valueLabel = "TOTAL CLOSET INVESTMENT", // TODO: Move to strings
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
                            text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_hub_blueprint_history),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
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

data class CuratedCollectionUiState(val analysis: SavedAnalysis)

@Composable
private fun CuratedCollectionCard(
    uiState: CuratedCollectionUiState,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault()) }
    val dateStr = dateFormat.format(java.util.Date(analysis.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEvent() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F2F8)) 
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = analysis.advice.seasonalType.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Top) {
                Row(modifier = Modifier.weight(0.4f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    analysis.advice.faceUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    analysis.advice.hairUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = analysis.advice.title ?: stringResource(com.zoewave.probase.kocolor.mobile.features.color.R.string.applications_kocolor_apps_mobile_features_color_curated_look),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = analysis.advice.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.6f),
                    color = Color.DarkGray
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analysis.advice.recommendedPalette.take(5).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(com.zoewave.probase.features.graphics.colorpicker.util.parseColor(hex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
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
    val breakdown: Map<String, Int> = emptyMap(),
    val modifier: Modifier = Modifier
)

@Composable
private fun ArchiveVerticalCard(
    uiState: ArchiveVerticalUiState,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = uiState.modifier
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
