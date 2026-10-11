package com.zoewave.probase.kocolor.mobile.features.color.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.db.entity.PlaylistWithDays
import com.zoewave.probase.kocolor.mobile.features.color.R
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun ColorUiRoutePreview() {
    MaterialTheme {
        ColorUiRoute(
            uiState = ColorUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun ColorUiRoute(
    uiState: ColorUiState,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ColorScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorScreen(
    uiState: ColorUiState,
    modifier: Modifier = Modifier,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.applications_kocolor_apps_mobile_features_color_analysis_history), 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F6F0),
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 0. Archive Title Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = "KOCOLOR ARCHIVE • CHROMATIC ADVISORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF8A7165)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Color Analysis History",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1B24)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Curated aesthetic recommendations saved to your chromatic profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A736E)
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(
                        color = Color(0xFFEFE8E1),
                        thickness = 1.dp
                    )
                }
            }

            // 1. AI Fashion Advisor (Style Simulator)
            item {
                AiFashionAdvisorSummaryCard(
                    onPrimaryClick = { navTo(KoColorRoute.StyleSimulator) },
                    onListClick = { navTo(KoColorRoute.CollectionHub(filter = "LOOKS")) }
                )
            }

            // 2. Destination Capsule Architect (Travel Mode)
            item {
                TravelCapsuleSummaryCard(
                    onPrimaryClick = { navTo(KoColorRoute.TravelCapsule) },
                    onListClick = { navTo(KoColorRoute.CollectionHub(filter = "TRAVEL")) }
                )
            }

            // 3. Style Playlist Summary (7-Day Forecast)
            item {
                StylePlaylistSummaryCard(
                    playlist = uiState.latestPlaylist,
                    onPrimaryClick = { navTo(KoColorRoute.StylePlaylist) },
                    onListClick = { navTo(KoColorRoute.StylePlaylist) }
                )
            }

            // 4. Section Divider/Title for Recent History (7 Most Recent)
            if (uiState.savedSuggestions.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENT SAVED COLLECTIONS (TOP 7)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            if (uiState.savedSuggestions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_no_history), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(uiState.savedSuggestions.take(7)) { analysis ->
                    ColorHistoryCard(
                        uiState = ColorHistoryCardUiState(analysis),
                        onEvent = { event ->
                            when (event) {
                                is ColorHistoryEvent.Click -> navTo(KoColorRoute.CollectionDetail(analysis.id))
                                is ColorHistoryEvent.Edit -> navTo(KoColorRoute.Stitch(analysis.id, isCopy = false))
                                is ColorHistoryEvent.Copy -> navTo(KoColorRoute.Stitch(analysis.id, isCopy = true))
                                is ColorHistoryEvent.Delete -> onEvent(ColorEvent.DeleteCollection(analysis.id))
                            }
                        },
                        navTo = navTo
                    )
                }

                if (uiState.savedSuggestions.size > 7) {
                    item {
                        OutlinedButton(
                            onClick = { navTo(KoColorRoute.CollectionHub()) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("View All (${uiState.savedSuggestions.size}) Collections", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. Contextual Intelligence Disclaimer Footer
            item {
                Text(
                    text = "* Contextual Intelligence: Dynamic calculations based on your chromatic profile.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E8E81),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AnimatedActionBadge(
    icon: ImageVector,
    badgeColor: Color,
    iconTint: Color,
    auraColors: List<Color>,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BadgeSparkleAnimation")

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeIconScale"
    )

    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeAuraScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeAuraAlpha"
    )

    val auraBrush = Brush.sweepGradient(auraColors)

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .graphicsLayer {
                    scaleX = auraScale
                    scaleY = auraScale
                    alpha = auraAlpha
                }
                .clip(CircleShape)
                .background(auraBrush)
        )

        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = badgeColor.copy(alpha = 0.95f),
            border = BorderStroke(1.5.dp, Color.White),
            shadowElevation = 4.dp,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, iconTint.copy(alpha = 0.25f), CircleShape)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }
        }
    }
}

@Composable
private fun AiFashionAdvisorSummaryCard(
    onPrimaryClick: () -> Unit,
    onListClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Top Badge & Icon Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF3EDF7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✦  AI FASHION ADVISOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                AnimatedActionBadge(
                    icon = Icons.Default.AutoAwesome,
                    badgeColor = Color(0xFFF3EDF7),
                    iconTint = Color(0xFF6750A4),
                    auraColors = listOf(Color(0xFFD4AF37), Color(0xFFE91E63), Color(0xFF8E24AA), Color(0xFF6750A4), Color(0xFFD4AF37)),
                    onClick = onPrimaryClick
                )
            }

            Spacer(Modifier.height(16.dp))

            // Headline
            Text(
                "Define Your Intent",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1B24)
            )

            Spacer(Modifier.height(8.dp))

            // Body
            Text(
                text = "Generate a personalized outfit and cosmetic coordination from your vault based on daily mood and constraints.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5A5760),
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(16.dp))

            // Thin Horizontal Divider Line
            HorizontalDivider(
                color = Color(0xFFF3F0EC),
                thickness = 1.dp
            )

            Spacer(Modifier.height(20.dp))

            // Two Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Gradient Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A347F), Color(0xFF6246A3))
                            ),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { onPrimaryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Define Intent",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "↗",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Secondary Outlined Pill Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { onListClick() },
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE6E0EC))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color(0xFF4A347F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Saved Looks",
                            color = Color(0xFF1C1B1F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Subtext Footer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "• Color Formula DNA: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8F8A98),
                    fontSize = 11.sp
                )
                Text(
                    "Soft Autumn",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1C1B1F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    "  • 112 Vault Items",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8F8A98),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun TravelCapsuleSummaryCard(
    onPrimaryClick: () -> Unit,
    onListClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Top Badge & Icon Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F1FF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✦  DESTINATION CAPSULE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5),
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                AnimatedActionBadge(
                    icon = Icons.Default.Flight,
                    badgeColor = Color(0xFFE8F1FF),
                    iconTint = Color(0xFF1E88E5),
                    auraColors = listOf(Color(0xFF00BCD4), Color(0xFF1E88E5), Color(0xFF1565C0), Color(0xFF00BCD4)),
                    onClick = onPrimaryClick
                )
            }

            Spacer(Modifier.height(16.dp))

            // Headline
            Text(
                "Travel Packing AI",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1B24)
            )

            Spacer(Modifier.height(8.dp))

            // Body
            Text(
                text = "Generate a curated wardrobe and cosmetic plan for your next international voyage.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5A5760),
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(16.dp))

            // Thin Horizontal Divider Line
            HorizontalDivider(
                color = Color(0xFFF3F0EC),
                thickness = 1.dp
            )

            Spacer(Modifier.height(20.dp))

            // Two Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Gradient Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
                            ),
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { onPrimaryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Plan Trip",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "↗",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Secondary Outlined Pill Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { onListClick() },
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFD6E4FF))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Saved Trips",
                            color = Color(0xFF1C1B1F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Subtext Footer
            Text(
                "• Weather & UV Synced • Destination Temperature Matching",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8F8A98),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StylePlaylistSummaryCard(
    playlist: PlaylistWithDays?,
    isActive: Boolean = false,
    onPrimaryClick: () -> Unit,
    onListClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isActive) 1f else 0.7f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "STYLE PLAYLIST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF6750A4)
                    )
                    Text(
                        "7-Day Forecast",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Automated 7-day wardrobe and cosmetic rotation based on weather forecasts.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Build Forecast", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onListClick,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("View Playlists", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}


data class ColorHistoryCardUiState(val analysis: SavedAnalysis)

sealed class ColorHistoryEvent {
    data object Click : ColorHistoryEvent()
    data object Edit : ColorHistoryEvent()
    data object Copy : ColorHistoryEvent()
    data object Delete : ColorHistoryEvent()
}

@Composable
private fun ColorHistoryCard(
    uiState: ColorHistoryCardUiState,
    modifier: Modifier = Modifier,
    onEvent: (ColorHistoryEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(analysis.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEvent(ColorHistoryEvent.Click) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = analysis.advice.seasonalType.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { onEvent(ColorHistoryEvent.Copy) }) {
                        Icon(Icons.Default.ContentCopy, stringResource(R.string.applications_kocolor_apps_mobile_features_color_copy), modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    IconButton(onClick = { onEvent(ColorHistoryEvent.Edit) }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.applications_kocolor_apps_mobile_features_color_edit), modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    IconButton(onClick = { onEvent(ColorHistoryEvent.Delete) }) {
                        Icon(Icons.Default.DeleteOutline, stringResource(R.string.applications_kocolor_apps_mobile_features_color_delete), modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(0.45f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    analysis.advice.faceUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    analysis.advice.clothesUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(0.55f)) {
                    Text(
                        text = analysis.advice.title ?: stringResource(R.string.applications_kocolor_apps_mobile_features_color_curated_look),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = analysis.advice.summary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF2C2420)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analysis.advice.recommendedPalette.take(5).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}

data class ColorDetailUiState(
    val analysis: SavedAnalysis? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDetailScreen(
    uiState: ColorDetailUiState,
    modifier: Modifier = Modifier,
    onEvent: (ColorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis ?: return
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_analysis_details)) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_apps_mobile_features_color_back))
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                analysis.advice.faceUri?.let {
                    Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Face",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                analysis.advice.clothesUri?.let {
                    Card(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        AsyncImage(
                            model = it,
                            contentDescription = "Clothes",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_seasonal_type), style = MaterialTheme.typography.labelMedium)
                        Text(analysis.advice.seasonalType.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_undertone), style = MaterialTheme.typography.labelMedium)
                        Text(analysis.advice.undertone.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.applications_kocolor_apps_mobile_features_color_makeup_palette),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MakeupPaletteGraphic(
                uiState = analysis.advice.recommendedPalette,
                onEvent = {},
                navTo = navTo
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.applications_kocolor_apps_mobile_features_color_makeup_nail_suggestions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            analysis.advice.makeupSuggestions.forEach { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(suggestion.category, fontWeight = FontWeight.Bold)
                            Text(suggestion.advice)
                        }
                        if (suggestion.category.contains("Nail", ignoreCase = true)) {
                            Button(
                                onClick = {
                                    val color = suggestion.recommendedColors.firstOrNull() ?: "#FF0000"
                                    val finish = if (suggestion.advice.contains("Matte", ignoreCase = true)) "MATTE"
                                                 else if (suggestion.advice.contains("Metallic", ignoreCase = true)) "METALLIC"
                                                 else "GLOSSY"
                                    // navTo(KoColorRoute.NailLab(color, finish))
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.applications_kocolor_apps_mobile_features_color_experience), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.applications_kocolor_apps_mobile_features_color_ai_coordination_notes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = analysis.advice.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MakeupPaletteGraphic(
    uiState: List<String>,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val paletteBackground = Brush.linearGradient(
        colors = listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(paletteBackground).padding(16.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(uiState) { hex ->
                    MakeupPan(
                        uiState = hex,
                        onEvent = {},
                        navTo = navTo
                    )
                }
            }
        }
    }
}

@Composable
fun MakeupPan(
    uiState: String,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(uiState))
    } catch (e: Exception) {
        Color.Gray
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
        Text(
            text = uiState.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeupPanPreview() {
    MaterialTheme {
        MakeupPan(uiState = "#FF0000", onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeupPaletteGraphicPreview() {
    MaterialTheme {
        MakeupPaletteGraphic(uiState = listOf("#FF0000", "#00FF00", "#0000FF"), onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorHistoryCardPreview() {
    MaterialTheme {
        ColorHistoryCard(
            uiState = ColorHistoryCardUiState(
                SavedAnalysis(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    advice = FashionAdvice(
                        summary = "Your features—cool-toned skin, dark hair, and clear eyes—align perfectly with a Deep Winter palette.",
                        seasonalType = SeasonalType.WINTER,
                        undertone = Undertone.COOL,
                        makeupSuggestions = emptyList(),
                        outfitSuggestions = emptyList(),
                        recommendedPalette = listOf("#0047AB", "#FFFFFF", "#708090", "#C77398", "#3D2B1F")
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorScreenPreview_Populated() {
    MaterialTheme {
        ColorScreen(
            uiState = ColorUiState(
                savedSuggestions = listOf(
                    SavedAnalysis(
                        id = 1,
                        timestamp = System.currentTimeMillis(),
                        advice = FashionAdvice(
                            summary = "Soft summer look with pastels.",
                            seasonalType = SeasonalType.SUMMER,
                            undertone = Undertone.NEUTRAL,
                            makeupSuggestions = emptyList(),
                            outfitSuggestions = emptyList(),
                            recommendedPalette = listOf("#F8D7DA", "#D4EDDA", "#CCE5FF")
                        )
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorDetailScreenPreview() {
    MaterialTheme {
        ColorDetailScreen(
            uiState = ColorDetailUiState(
                analysis = SavedAnalysis(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    advice = FashionAdvice(
                        summary = "Perfect coordination for a night out.",
                        seasonalType = SeasonalType.WINTER,
                        undertone = Undertone.COOL,
                        makeupSuggestions = emptyList(),
                        outfitSuggestions = emptyList(),
                        recommendedPalette = listOf("#1A1A1A", "#FFFFFF", "#C0C0C0", "#FF007F", "#4B0082", "#000080")
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
