package com.zoewave.probase.kocolor.mobile.features.color.ui

import com.zoewave.probase.kocolor.mobile.features.color.ui.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
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
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.text.SimpleDateFormat
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
    val savedTripsCount = remember(uiState.savedSuggestions) {
        uiState.savedSuggestions.count { it.advice.title?.contains("Trip", ignoreCase = true) == true }
    }
    val savedLooksCount = remember(uiState.savedSuggestions) {
        uiState.savedSuggestions.count { it.advice.title?.contains("Trip", ignoreCase = true) != true }
    }

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
                    savedCount = savedLooksCount,
                    onPrimaryClick = { navTo(KoColorRoute.StyleSimulator) },
                    onListClick = { navTo(KoColorRoute.CollectionHub(filter = "LOOKS")) }
                )
            }

            // 2. Destination Capsule Architect (Travel Mode)
            item {
                TravelCapsuleSummaryCard(
                    savedCount = savedTripsCount,
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
    badgeGradient: Brush,
    iconTint: Color = Color.White,
    auraColors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 3D Tactile Press Scale Effect
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "Badge3DPressScale"
    )

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
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeAuraScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeAuraAlpha"
    )

    val auraBrush = Brush.sweepGradient(auraColors)

    // Top-down specular light gradient for true 3D curvature
    val specularLightGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.55f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.35f)
        )
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Outer Prismatic Multi-Chromatic Aura Glow Ring
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = auraScale
                    scaleY = auraScale
                    alpha = auraAlpha
                }
                .clip(CircleShape)
                .background(auraBrush)
        )

        // 2. Main 3D Sphere Interactive Orb
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(elevation = 10.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.5f))
                .background(brush = badgeGradient, shape = CircleShape)
                // 3D Specular Light Bevel Overlay
                .background(brush = specularLightGradient, shape = CircleShape)
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(Color.White, Color.White.copy(alpha = 0.3f))
                        )
                    ),
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 28.dp),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Recessed Inner Bevel Ring
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.40f), CircleShape)
            )

            // Sparkling 3D Floating Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
        }
    }
}

@Composable
private fun AiFashionAdvisorSummaryCard(
    savedCount: Int = 0,
    onPrimaryClick: () -> Unit,
    onListClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF6EEF8),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.65f), Color(0xFFF3E7F5)),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill & Floating Badge
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    // Translucent Frosted Glass Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, end = 12.dp)
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Define Your Intent",
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1B24),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Floating 3D Animated Action Badge (Intersecting Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (4).dp, y = (-2).dp)
                    ) {
                        AnimatedActionBadge(
                            icon = Icons.Default.AutoAwesome,
                            badgeGradient = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A347F), Color(0xFF6246A3))
                            ),
                            iconTint = Color.White,
                            auraColors = listOf(Color(0xFFD4AF37), Color(0xFFE91E63), Color(0xFF8E24AA), Color(0xFF6750A4), Color(0xFFD4AF37)),
                            onClick = onPrimaryClick
                        )
                    }
                }

                // BOTTOM HALF: Subtitle / Chevron Toggle Row
                val looksText = when (savedCount) {
                    0 -> "None Saved"
                    1 -> "1 Saved Look"
                    else -> "$savedCount Saved Looks"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = looksText,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = if (savedCount > 0) Color(0xFF6750A4) else Color(0xFF1F2937),
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }

                // COLLAPSIBLE CONTENT
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Generate a personalized outfit and cosmetic coordination from your vault based on daily mood and constraints.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5A5760),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Translucent Divider Line
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.7f),
                            thickness = 1.dp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Bottom Section: Left Metadata, Right Glassy Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LEFT: Info
                            Column(horizontalAlignment = Alignment.Start) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DNA: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7A7382),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Soft Autumn",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1C1B1F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = looksText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (savedCount > 0) Color(0xFF6750A4) else Color(0xFF7A7382),
                                    fontWeight = if (savedCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }

                            // RIGHT: Glassy Button
                            Surface(
                                onClick = onListClick,
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, Color.White),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Saved Looks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1C1B1F)
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

@Composable
private fun TravelCapsuleSummaryCard(
    savedCount: Int = 0,
    onPrimaryClick: () -> Unit,
    onListClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFEEF5FF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Android Best Practice for Card Background Image:
            // 1. Hardware Accelerated & Async-loaded via Coil (AsyncImage)
            // 2. Translucent Alpha Scrim (0.28f) to ensure background texture does not clash with foreground text
            // 3. ContentScale.Crop for edge-to-edge coverage within the card bounds
            AsyncImage(
                model = com.zoewave.probase.kocolor.features.travel.R.drawable.kocolorplane,
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.92f),
                contentScale = ContentScale.Crop
            )

            // Radial Tint Overlay for contrast & luxury aesthetic
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.55f), Color(0xFFE1EDFF).copy(alpha = 0.75f)),
                            radius = 800f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill & Floating Badge
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    // Translucent Frosted Glass Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, end = 12.dp)
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Travel Packing AI",
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1B24),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Floating 3D Animated Action Badge (Intersecting Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (4).dp, y = (-2).dp)
                    ) {
                        AnimatedActionBadge(
                            icon = Icons.Default.Flight,
                            badgeGradient = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
                            ),
                            iconTint = Color.White,
                            auraColors = listOf(Color(0xFF00BCD4), Color(0xFF1E88E5), Color(0xFF1565C0), Color(0xFF00BCD4)),
                            onClick = onPrimaryClick
                        )
                    }
                }

                // BOTTOM HALF: Subtitle / Chevron Toggle Row
                val tripsText = when (savedCount) {
                    0 -> "None Saved"
                    1 -> "1 Saved Trip"
                    else -> "$savedCount Saved Trips"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tripsText,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = if (savedCount > 0) Color(0xFF1E88E5) else Color(0xFF1F2937),
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }

                // COLLAPSIBLE CONTENT
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Generate a curated wardrobe and cosmetic plan for your next international voyage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5A5760),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Translucent Divider Line
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.7f),
                            thickness = 1.dp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Bottom Section: Left Metadata, Right Glassy Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LEFT: Info
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "Weather & UV Synced",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1C1B1F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = tripsText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (savedCount > 0) Color(0xFF1E88E5) else Color(0xFF7A7382),
                                    fontWeight = if (savedCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }

                            // RIGHT: Glassy Button
                            Surface(
                                onClick = onListClick,
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, Color.White),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color(0xFF1E88E5),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Saved Trips",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1C1B1F)
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

@Composable
private fun StylePlaylistSummaryCard(
    playlist: PlaylistWithDays?,
    isActive: Boolean = false,
    onPrimaryClick: () -> Unit,
    onListClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF3ECEF),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.65f), Color(0xFFEFE8EB)),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill & Floating Badge
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    // Translucent Frosted Glass Pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, end = 12.dp)
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "7-Day Forecast",
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1B24),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Floating 3D Animated Action Badge (Intersecting Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (4).dp, y = (-2).dp)
                    ) {
                        AnimatedActionBadge(
                            icon = Icons.Default.AutoAwesome,
                            badgeGradient = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6750A4), Color(0xFF8E24AA))
                            ),
                            iconTint = Color.White,
                            auraColors = listOf(Color(0xFFD4AF37), Color(0xFF8E24AA), Color(0xFF6750A4), Color(0xFFD4AF37)),
                            onClick = onPrimaryClick
                        )
                    }
                }

                // BOTTOM HALF: Subtitle / Chevron Toggle Row
                val playlistText = if (playlist != null) "1 Active Playlist" else "None Saved"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = playlistText,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = if (playlist != null) Color(0xFF6750A4) else Color(0xFF1F2937),
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation)
                    )
                }

                // COLLAPSIBLE CONTENT
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Automated 7-day wardrobe and cosmetic rotation based on weather forecasts.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5A5760),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Translucent Divider Line
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.7f),
                            thickness = 1.dp
                        )

                        Spacer(Modifier.height(14.dp))

                        // Bottom Section: Left Metadata, Right Glassy Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LEFT: Info
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "7-Day Rotation Plan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1C1B1F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = playlistText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (playlist != null) Color(0xFF6750A4) else Color(0xFF7A7382),
                                    fontWeight = if (playlist != null) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }

                            // RIGHT: Glassy Button
                            Surface(
                                onClick = onListClick,
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, Color.White),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "View Playlists",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1C1B1F)
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


