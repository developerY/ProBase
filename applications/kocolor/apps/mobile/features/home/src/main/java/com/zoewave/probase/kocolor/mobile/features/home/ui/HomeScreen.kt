package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun HomeUiRoute(
    uiState: WindowSizeClass,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (uiState.isDaytime) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 1500),
        label = "ChronobiologicalBackground"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { LuxuryBrandLogo() },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                HomeHeader(
                    uiState = uiState.fashionProfile,
                    isDaytime = uiState.isDaytime,
                    beautyTip = uiState.beautyTip
                )
            }

            item {
                WellnessInsightsSection(
                    insights = uiState.wellnessInsights,
                    sleepDuration = uiState.lastNightSleepDuration,
                    hydrationLiters = uiState.hydrationLiters,
                    hydrationGoalLiters = uiState.hydrationGoalLiters,
                    isPermissionGranted = uiState.isHealthPermissionGranted,
                    navTo = navTo
                )
            }

            item {
                QuickActions(navTo = navTo)
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine
                val title = if (uiState.isDaytime) "Morning Ritual" else "Evening Ritual"
                
                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(title = title, subtitle = "Your bio-synced ritual")
                        RoutineSummaryCard(
                            routine = routine,
                            isDaytime = uiState.isDaytime,
                            onClick = { navTo(KoColorRoute.Routines) }
                        )
                    }
                }
            }

            if (uiState.totalCosmetics > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(title = "The Vanity", subtitle = "${uiState.totalCosmetics} items tracked")
                        InventoryDashboard(uiState = uiState, navTo = navTo)
                    }
                }
            }

            if (uiState.totalClothing > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(title = "The Wardrobe", subtitle = "${uiState.totalClothing} pieces curated")
                        WardrobeDashboard(uiState = uiState, navTo = navTo)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
private fun LuxuryBrandLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "LuxuryLight")
    val lightX by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightX"
    )

    // Calculate shadow offset based on light position
    val shadowOffsetX = -(lightX / 10).dp
    val shadowAlpha = (0.3f - (kotlin.math.abs(lightX) / 1000f)).coerceAtLeast(0.1f)

    Box(contentAlignment = Alignment.Center) {
        // Subtle moving light source (Sun)
        Box(
            modifier = Modifier
                .offset { IntOffset(lightX.toInt(), -20) }
                .size(40.dp)
                .blur(20.dp)
                .background(Color(0xFFFFF9C4).copy(alpha = 0.4f), CircleShape)
        )

        Text(
            text = "KoColor",
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    offset = androidx.compose.ui.geometry.Offset(shadowOffsetX.value, 4f),
                    blurRadius = 8f
                )
            ),
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HomeHeader(
    uiState: FashionProfile?,
    isDaytime: Boolean,
    beautyTip: String
) {
    val gradientColors = if (isDaytime) {
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
    } else {
        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.surfaceVariant)
    }

    val expressiveShape = RoundedCornerShape(topStart = 48.dp, topEnd = 12.dp, bottomEnd = 48.dp, bottomStart = 12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(expressiveShape)
            .background(Brush.linearGradient(colors = gradientColors))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), expressiveShape)
            .padding(32.dp)
    ) {
        Column {
            Text(
                text = if (isDaytime) "Radiant Morning." else "Deep Restoration.",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(0.8f)) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = beautyTip, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            if (uiState != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                        Text(text = uiState.seasonalType.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "· ${uiState.undertone.name.lowercase().replaceFirstChar { it.uppercase() }} Undertone", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WellnessInsightsSection(
    insights: List<SkinInsight>,
    sleepDuration: String?,
    hydrationLiters: Double,
    hydrationGoalLiters: Double,
    isPermissionGranted: Boolean,
    navTo: (KoColorRoute) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(title = "Bio-Markers", subtitle = "Style from the inside out")
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().clickable { navTo(KoColorRoute.Health) },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (!isPermissionGranted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Sync Health Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "Tap to connect your style vitals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        BioMarkerItem(icon = Icons.Default.Bedtime, label = "Sleep", value = sleepDuration ?: "--", color = Color(0xFF9C27B0), modifier = Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(icon = Icons.Default.WaterDrop, label = "Hydration", value = "%.1fL".format(hydrationLiters), color = Color(0xFF2196F3), modifier = Modifier.weight(1f))
                        VerticalDivider(modifier = Modifier.height(48.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        BioMarkerItem(icon = Icons.Default.AutoAwesome, label = "Vitals", value = if (insights.isEmpty()) "Optimal" else "${insights.size} Alerts", color = if (insights.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336), modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun BioMarkerItem(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = color) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
    }
}

@Composable
private fun QuickActions(navTo: (KoColorRoute) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        QuickActionCard(title = "Analyze Style", subtitle = "AI Visual Analysis", icon = Icons.Default.AutoAwesome, color = MaterialTheme.colorScheme.primary, onClick = { navTo(KoColorRoute.StyleSimulator) }, modifier = Modifier.weight(1f))
        QuickActionCard(title = "Capture Product", subtitle = "Gemini Scanner", icon = Icons.Default.CameraAlt, color = MaterialTheme.colorScheme.secondary, onClick = { navTo(KoColorRoute.Analyzer()) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RoutineSummaryCard(routine: BeautyRoutine, isDaytime: Boolean, onClick: () -> Unit) {
    val completedCount = routine.steps.count { it.isCompleted }
    val totalCount = routine.steps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val nextStep = routine.steps.sortedBy { it.layeringOrder }.find { !it.isCompleted }
    val cardColor = if (isDaytime) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant

    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(32.dp), color = cardColor) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val displayObjective = routine.biologicalObjective ?: routine.time.biologicalObjective
                Text(text = "Objective: $displayObjective", style = MaterialTheme.typography.labelMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Next Step", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = nextStep?.title ?: "Ritual Complete", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                    CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), strokeWidth = 5.dp)
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary, strokeWidth = 5.dp, strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                    Text(text = "$completedCount/$totalCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun InventoryDashboard(uiState: HomeUiState, navTo: (KoColorRoute) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), onClick = { navTo(KoColorRoute.VanityLanding) }) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Face, null, modifier = Modifier.size(160.dp).align(Alignment.CenterEnd).offset(x = 40.dp, y = 40.dp).alpha(0.05f), tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = uiState.totalCosmetics.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text(text = "TOTAL PRODUCTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.popularCosmetics.forEach { item ->
                        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant).border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = item.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeDashboard(uiState: HomeUiState, navTo: (KoColorRoute) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), onClick = { navTo(KoColorRoute.WardrobeLanding) }) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Checkroom, null, modifier = Modifier.size(160.dp).align(Alignment.CenterEnd).offset(x = 40.dp, y = 40.dp).alpha(0.05f), tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(24.dp)) {
                Column {
                    Text(text = uiState.totalClothing.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text(text = "TOTAL PIECES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.popularClothing.forEach { item ->
                        Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant).border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = item.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
        Text(text = subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(uiState = HomeUiState(), onEvent = {}, navTo = {})
}
