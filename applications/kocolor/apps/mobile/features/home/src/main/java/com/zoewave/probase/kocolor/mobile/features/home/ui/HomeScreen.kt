package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.health.core.SkinInsight
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

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
    // Smoothly animate the background based on time of day
    val backgroundColor by animateColorAsState(
        targetValue = if (uiState.isDaytime) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 1500),
        label = "ChronobiologicalBackground"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "KoColor", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                HomeHeader(
                    uiState = uiState.fashionProfile,
                    isDaytime = uiState.isDaytime
                )
            }

            item {
                WellnessInsightsSection(
                    insights = uiState.wellnessInsights,
                    sleepDuration = uiState.lastNightSleepDuration,
                    isPermissionGranted = uiState.isHealthPermissionGranted,
                    navTo = navTo
                )
            }

            item {
                BeautyTipSection(uiState.beautyTip)
            }

            item {
                QuickActions(navTo = navTo)
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine
                val title = if (uiState.isDaytime) "Morning Ritual" else "Evening Ritual"
                
                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(
                            title = title,
                            subtitle = "Your bio-synced ritual"
                        )
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
                        SectionTitle(
                            title = "The Vanity",
                            subtitle = "${uiState.totalCosmetics} items tracked"
                        )
                        InventoryDashboard(
                            uiState = uiState,
                            navTo = navTo
                        )
                    }
                }
            }

            if (uiState.totalClothing > 0) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        SectionTitle(
                            title = "The Wardrobe",
                            subtitle = "${uiState.totalClothing} pieces curated"
                        )
                        WardrobeDashboard(
                            uiState = uiState,
                            navTo = { navTo(KoColorRoute.Wardrobe) }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun HomeHeader(
    uiState: FashionProfile?,
    isDaytime: Boolean
) {
    val gradientColors = if (isDaytime) {
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface)
    } else {
        listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.surfaceVariant)
    }

    val expressiveShape = RoundedCornerShape(
        topStart = 48.dp,
        topEnd = 12.dp,
        bottomEnd = 48.dp,
        bottomStart = 12.dp
    )

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
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = uiState.seasonalType.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "· ${uiState.undertone.name.lowercase().replaceFirstChar { it.uppercase() }} Undertone",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WellnessInsightsSection(
    insights: List<SkinInsight>,
    sleepDuration: String?,
    isPermissionGranted: Boolean,
    navTo: (KoColorRoute) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(
            title = "Biological Context",
            subtitle = "Vital skin insights"
        )
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
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
fun BeautyTipSection(uiState: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = uiState, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif)
        }
    }
}

@Composable
fun QuickActions(navTo: (KoColorRoute) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { navTo(KoColorRoute.Analyzer()) },
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Analyze", style = MaterialTheme.typography.titleMedium)
        }
        
        OutlinedButton(
            onClick = { navTo(KoColorRoute.Cosmetics()) },
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Inventory, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Vanity", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun RoutineSummaryCard(
    routine: BeautyRoutine,
    isDaytime: Boolean,
    onClick: () -> Unit
) {
    val completedCount = routine.steps.count { it.isCompleted }
    val totalCount = routine.steps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val nextStep = routine.steps.sortedBy { it.layeringOrder }.find { !it.isCompleted }

    val cardColor = if (isDaytime) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.surfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        color = cardColor
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    val displayObjective = routine.biologicalObjective ?: routine.time.biologicalObjective

                    Text(
                        text = "Objective: $displayObjective",
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    if (routine.contextFactors.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = routine.contextFactors.first().uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Next Step",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nextStep?.title ?: "Ritual Complete",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (nextStep != null && nextStep.minWaitMinutes > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wait ${nextStep.minWaitMinutes} mins to absorb",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        strokeWidth = 5.dp
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 5.dp,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Text(
                        text = "$completedCount/$totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryDashboard(
    uiState: HomeUiState,
    navTo: (KoColorRoute) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Advanced Horizontal Collection (The Vanity)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        ) {
            items(uiState.popularCosmetics) { item ->
                VanityProductCard(
                    uiState = item,
                    onClick = { navTo(KoColorRoute.Cosmetics()) } // Maybe detail later
                )
            }

            item {
                ViewAllCard(
                    itemCount = uiState.totalCosmetics,
                    onClick = { navTo(KoColorRoute.Cosmetics()) }
                )
            }
        }
        
        // Category Breakdown Quick-Access
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val sections = listOf(
                "Face" to Icons.Default.Face,
                "Eyes" to Icons.Default.Visibility,
                "Lips" to Icons.Default.Favorite,
                "Cheeks" to Icons.Default.AutoAwesome
            )
            
            sections.forEach { (name, icon) ->
                val count = uiState.cosmeticsByGroup.entries.find { it.key.contains(name, ignoreCase = true) }?.value ?: 0
                if (count > 0) {
                    AssistChip(
                        onClick = { navTo(KoColorRoute.Cosmetics(filter = name)) },
                        label = { 
                            Text(
                                text = "$count $name",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            ) 
                        },
                        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            leadingIconContentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VanityProductCard(
    uiState: CosmeticItem,
    onClick: () -> Unit
) {
    val bgColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (uiState.colorHex != null) {
        if (isColorDark(bgColor)) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val costPerUse = uiState.costPerUse
    
    // Professional Expiry Logic
    val isExpiringSoon = uiState.estimatedExpiry?.let { expiry ->
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        (expiry - System.currentTimeMillis()) in 0..thirtyDaysInMillis
    } ?: false

    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 16.dp, 
                        shape = RoundedCornerShape(28.dp), 
                        spotColor = bgColor.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = bgColor,
                border = BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.1f))
            ) {
                if (uiState.imageUrl != null) {
                    AsyncImage(
                        model = uiState.imageUrl,
                        contentDescription = uiState.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (uiState.category == CosmeticCategory.AI_PENDING) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp), 
                            strokeWidth = 2.dp,
                            color = contentColor.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    // Modern placeholder for items without images
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // High-End Status Overlay
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isExpiringSoon) {
                    Surface(
                        color = Color.Red,
                        shape = CircleShape,
                        modifier = Modifier.size(10.dp).border(1.5.dp, Color.White, CircleShape)
                    ) {}
                }
                
                if (uiState.isOpened) {
                    Surface(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            "OPEN", 
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
            
            // Usage Counter Badge (Bottom Left)
            if (uiState.usageCount > 0) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${uiState.usageCount}x",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Brand Label (Minimalist & Professional)
        Text(
            text = uiState.brand.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )

        Text(
            text = uiState.name,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.category.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            if (costPerUse != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$%.2f".format(costPerUse),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ViewAllCard(itemCount: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(width = 140.dp, height = 175.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "View All",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$itemCount tracked",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WardrobeDashboard(
    uiState: HomeUiState,
    navTo: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        onClick = navTo
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
                }
            }
        }
    }
}

@Composable
fun CompactProductCard(uiState: CosmeticItem) {
    val bgColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant
    val costPerUse = uiState.costPerUse

    // Check if the product is expiring within the next 30 days
    val isExpiringSoon = uiState.estimatedExpiry?.let { expiry ->
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        (expiry - System.currentTimeMillis()) in 0..thirtyDaysInMillis
    } ?: false

    Column(
        modifier = Modifier.width(120.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), spotColor = bgColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                color = bgColor
            ) {
                if (uiState.imageUrl != null) {
                    AsyncImage(
                        model = uiState.imageUrl,
                        contentDescription = uiState.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (uiState.category == CosmeticCategory.AI_PENDING) {
                    Icon(
                        Icons.Default.DocumentScanner,
                        contentDescription = null,
                        modifier = Modifier.padding(32.dp).fillMaxSize(),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // Expiry Warning Badge
            if (isExpiringSoon) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(12.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.brand.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            // Display Cost Per Use if available
            if (costPerUse != null) {
                Text(
                    text = "$%.2f".format(costPerUse),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = uiState.name,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Serif,
            maxLines = 1
        )
        Text(
            text = uiState.category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun HomeHeaderPreview() {
    MaterialTheme {
        HomeHeader(uiState = null, isDaytime = true)
    }
}

@Preview(showBackground = true)
@Composable
private fun BeautyTipSectionPreview() {
    MaterialTheme {
        BeautyTipSection(uiState = "Tip of the day")
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionsPreview() {
    MaterialTheme {
        QuickActions(navTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    MaterialTheme {
        SectionTitle(title = "Title", subtitle = "Subtitle")
    }
}

@Preview(showBackground = true)
@Composable
private fun CompactProductCardPreview() {
    MaterialTheme {
        CompactProductCard(
            uiState = CosmeticItem(name = "Lipstick", brand = "Luxe", category = CosmeticCategory.LIPSTICK, colorHex = "#FF0000")
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
