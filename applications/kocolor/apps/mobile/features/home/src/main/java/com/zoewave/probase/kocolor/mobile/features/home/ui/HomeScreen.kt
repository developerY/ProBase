package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.*

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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "KoColor", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                HomeHeader(
                    uiState = uiState.fashionProfile,
                    onEvent = {},
                    navTo = {}
                )
            }

            item {
                WellnessInsightsSection(
                    uiState = Triple(uiState.wellnessInsights, uiState.lastNightSleepDuration, uiState.isHealthPermissionGranted),
                    onEvent = {},
                    navTo = navTo
                )
            }

            item {
                BeautyTipSection(
                    uiState = uiState.beautyTip,
                    onEvent = {},
                    navTo = {}
                )
            }

            item {
                QuickActions(
                    uiState = Unit,
                    onEvent = {},
                    navTo = navTo
                )
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine
                val title = if (uiState.isDaytime) "morning beautiful routine" else "Evening Ritual"
                val icon = if (uiState.isDaytime) Icons.Default.LightMode else Icons.Default.Nightlight
                
                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionTitle(
                            uiState = title to icon,
                            onEvent = {},
                            navTo = {}
                        )
                        RoutineSummaryCard(
                            uiState = routine,
                            onEvent = { stepId -> onEvent(HomeEvent.ToggleStep(routine, stepId)) },
                            navTo = { navTo(KoColorRoute.Routines) }
                        )
                    }
                }
            }

            if (uiState.totalCosmetics > 0) {
                item {
                    SectionTitle(
                        uiState = "Cosmetic Collection" to Icons.Default.Brush,
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    InventoryDashboard(
                        uiState = uiState,
                        onEvent = {},
                        navTo = { navTo(KoColorRoute.Cosmetics) }
                    )
                }
            }

            if (uiState.totalClothing > 0) {
                item {
                    SectionTitle(
                        uiState = "Wardrobe" to Icons.Default.Checkroom,
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    WardrobeDashboard(
                        uiState = uiState,
                        onEvent = {},
                        navTo = { navTo(KoColorRoute.Wardrobe) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun HomeHeader(
    uiState: FashionProfile?,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(gradient).padding(24.dp)) {
            Column {
                Text(
                    text = if (uiState == null) "Welcome!" else "Hello, Beautiful",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState == null) {
                    Text("Unlock your best look with our AI color analysis.")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(uiState.seasonalType.name, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.undertone} Undertone",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WellnessInsightsSection(
    uiState: Triple<List<com.zoewave.probase.features.health.core.SkinInsight>, String?, Boolean>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val (insights, sleepDuration, isPermissionGranted) = uiState
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(
            uiState = "Wellness & Skin" to Icons.Default.HealthAndSafety,
            onEvent = {},
            navTo = {}
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (!isPermissionGranted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Connect Health Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Grant access to sleep and stress data in Settings to unlock biological skin insights.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { navTo(KoColorRoute.Settings) }) {
                            Text("Go to Settings")
                        }
                    }
                } else if (insights.isEmpty() && sleepDuration == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.QueryStats, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Recent Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your health data from last night hasn't synced yet. Wear your tracker to bed for personalized insights!",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    if (sleepDuration != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bedtime, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Last Night: $sleepDuration sleep",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (insights.isEmpty()) {
                        Text(
                            text = "Your vitals look great! Your skin is in optimal condition for standard routines.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        insights.forEachIndexed { index, insight ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = insight.trigger,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = insight.manifestation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Recommendation: ${insight.recommendation}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                            if (index < insights.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BeautyTipSection(
    uiState: String,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = uiState, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun QuickActions(
    uiState: Unit,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { navTo(KoColorRoute.Analyzer()) },
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze", style = MaterialTheme.typography.titleMedium)
        }
        
        OutlinedButton(
            onClick = { navTo(KoColorRoute.Cosmetics) },
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Inventory, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Inventory", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun RoutineSummaryCard(
    uiState: BeautyRoutine,
    onEvent: (String) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val completedCount = uiState.steps.count { it.isCompleted }
    val totalCount = uiState.steps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navTo(KoColorRoute.Routines) },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount of $totalCount steps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Next: " + (uiState.steps.find { !it.isCompleted }?.title ?: "All done!"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InventoryDashboard(
    uiState: HomeUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        onClick = { navTo(KoColorRoute.Cosmetics) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header with Total Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Cosmetics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${uiState.totalCosmetics} Total Items",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Category Breakdown Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val groups = listOf(
                    "Face" to Icons.Default.Face,
                    "Eyes" to Icons.Default.Visibility,
                    "Lips" to Icons.Default.Favorite,
                    "Cheeks" to Icons.Default.AutoAwesome
                )
                
                groups.forEach { (name, icon) ->
                    val count = uiState.cosmeticsByGroup.entries.find { it.key.contains(name, ignoreCase = true) }?.value ?: 0
                    if (count > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "$count $name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // "Recently Added" Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENTLY ADDED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                HorizontalDivider(modifier = Modifier.weight(1f).padding(start = 16.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.popularCosmetics.forEach { item ->
                    CompactProductCard(
                        uiState = item,
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
        }
    }
}

@Composable
fun WardrobeDashboard(
    uiState: HomeUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        onClick = { navTo(KoColorRoute.Wardrobe) }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Decorative Icon
            Icon(
                Icons.Default.Checkroom,
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .alpha(0.05f),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Wardrobe",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "${uiState.totalClothing} Pieces",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.popularClothing.forEach { item ->
                        val colorHex = item.colorHex
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (colorHex != null) parseColor(colorHex)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.imageUrl != null) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (colorHex == null) {
                                Icon(Icons.Default.Checkroom, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                    
                    // "More" circle
                    if (uiState.totalClothing > 5) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+${uiState.totalClothing - 5}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
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
fun CompactProductCard(
    uiState: CosmeticItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val bgColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (uiState.colorHex != null) {
        if (isColorDark(bgColor)) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.imageUrl != null) {
                AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = uiState.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = uiState.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = uiState.brand,
            style = MaterialTheme.typography.bodyExtraSmall, // Custom or using caption style
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

// Helper for smaller text
val Typography.bodyExtraSmall: androidx.compose.ui.text.TextStyle
    get() = labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.sp)

@Composable
fun SectionTitle(
    uiState: Pair<String, ImageVector>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(uiState.second, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = uiState.first,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        TextButton(onClick = { /* Could navigate to full list */ }) {
            Text("See All", style = MaterialTheme.typography.labelMedium)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    MaterialTheme {
        HomeHeader(uiState = null, onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun BeautyTipSectionPreview() {
    MaterialTheme {
        BeautyTipSection(uiState = "Tip of the day", onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionsPreview() {
    MaterialTheme {
        QuickActions(uiState = Unit, onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    MaterialTheme {
        SectionTitle(uiState = "Title" to Icons.Default.Home, onEvent = {}, navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactProductCardPreview() {
    MaterialTheme {
        CompactProductCard(
            uiState = CosmeticItem(name = "Lipstick", brand = "Luxe", category = CosmeticCategory.LIPSTICK, colorHex = "#FF0000"),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview_NoProfile() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview_WithProfile() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState(
                fashionProfile = FashionProfile(
                    seasonalType = SeasonalType.WINTER,
                    undertone = Undertone.COOL,
                    notes = "Your best colors are deep blues and jewel tones."
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
