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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
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
                        text = "Search entire collection",
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
                        title = "The Vanity",
                        count = uiState.totalCosmetics,
                        countLabel = "items tracked",
                        valueLabel = "NET COLLECTION VALUE",
                        value = uiState.totalVanityValue,
                        imageModel = R.drawable.vanity_white_background,
                        icon = Icons.Default.Face,
                        breakdown = uiState.cosmeticsByGroup,
                        onClick = { navTo(KoColorRoute.VanityLanding) }
                    )
                }

                item {
                    ArchiveVerticalCard(
                        title = "The Wardrobe",
                        count = uiState.totalClothing,
                        countLabel = "pieces curated",
                        valueLabel = "TOTAL CLOSET INVESTMENT",
                        value = uiState.totalWardrobeValue,
                        imageModel = R.drawable.wardrobe_background,
                        icon = Icons.Default.Checkroom,
                        breakdown = uiState.clothingByCategory,
                        onClick = { navTo(KoColorRoute.WardrobeLanding) }
                    )
                }

                if (uiState.savedSuggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "BLUEPRINT HISTORY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(uiState.savedSuggestions) { analysis ->
                        CuratedCollectionCard(
                            analysis = analysis,
                            onClick = { navTo(KoColorRoute.CollectionDetail(analysis.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedCollectionCard(
    analysis: SavedAnalysis,
    onClick: () -> Unit
) {
    val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, yyyy - HH:mm", java.util.Locale.getDefault()) }
    val dateStr = dateFormat.format(java.util.Date(analysis.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F2F8)) // Soft Lavender Gray
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
                // Feature Images
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
                    text = analysis.advice.title ?: "Curated Look",
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

            // Palette
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

@Composable
private fun ArchiveVerticalCard(
    title: String,
    count: Int,
    countLabel: String,
    valueLabel: String,
    value: Double,
    imageModel: Any,
    icon: ImageVector,
    breakdown: Map<String, Int> = emptyMap(),
    onClick: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageModel,
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
                            text = title,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "$count $countLabel",
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
                            Icon(icon, null, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // More Info: Breakdown
                if (breakdown.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        breakdown.entries.sortedByDescending { it.value }.take(3).forEach { (cat, num) ->
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
                        text = currencyFormatter.format(value),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = valueLabel,
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
