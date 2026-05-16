package com.zoewave.probase.kocolor.mobile.features.home.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.BeautyRoutine
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.FashionProfile
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime
import com.zoewave.probase.kocolor.model.SeasonalType
import com.zoewave.probase.kocolor.model.Undertone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTimed(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                ExpressiveHomeHeader(
                    uiState = uiState.fashionProfile,
                    isDaytime = uiState.isDaytime
                )
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine

                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        EditorialSectionTitle(
                            title = routine.title,
                            subtitle = "Your bio-synced ritual"
                        )
                        ExpressiveRoutineCard(
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
                        EditorialSectionTitle(
                            title = "The Vanity",
                            subtitle = "${uiState.totalCosmetics} items tracked"
                        )
                        ExpressiveInventoryDashboard(
                            uiState = uiState,
                            navTo = { navTo(KoColorRoute.Cosmetics) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveHomeHeader(
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
                        text = "· ${uiState.undertone.name.lowercase().capitalize()} Undertone",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExpressiveRoutineCard(
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
fun ExpressiveInventoryDashboard(
    uiState: HomeUiState,
    navTo: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
    ) {
        items(uiState.popularCosmetics) { item ->
            EditorialProductCard(uiState = item)
        }

        item {
            Surface(
                modifier = Modifier
                    .size(width = 120.dp, height = 160.dp)
                    .clickable { navTo() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    // mainAxisAlignment = MainAxisAlignment.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "View All")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("View All", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun EditorialProductCard(uiState: CosmeticItem) {
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
            text = uiState.category.displayName, // Utilizing the new enum property!
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun EditorialSectionTitle(title: String, subtitle: String) {
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

// --- MOCK DATA FOR PREVIEWS ---

private fun getMockFashionProfile() = FashionProfile(
    seasonalType = SeasonalType.WINTER,
    undertone = Undertone.COOL,
    notes = "Deep, high-contrast cool tones."
)

private fun getMockMorningRoutine() = BeautyRoutine(
    id = 1L,
    title = "Morning Defense",
    time = RoutineTime.MORNING,
    date = System.currentTimeMillis(),
    contextFactors = listOf("High UV Index"),
    steps = listOf(
        RoutineStep(id = "s1", title = "Gentle Cleanser", description = "Prep skin", isCompleted = true, layeringOrder = 1, category = CosmeticCategory.OTHER),
        RoutineStep(id = "s2", title = "Vitamin C Serum", description = "Antioxidant", isCompleted = true, layeringOrder = 2, minWaitMinutes = 2, category = CosmeticCategory.OTHER),
        RoutineStep(id = "s3", title = "Moisturizer", description = "Hydrate", isCompleted = false, layeringOrder = 3, category = CosmeticCategory.OTHER),
        RoutineStep(id = "s4", title = "SPF 50+", description = "Protect", isCompleted = false, layeringOrder = 4, category = CosmeticCategory.OTHER)
    )
)

private fun getMockEveningRoutine() = BeautyRoutine(
    id = 2L,
    title = "Evening Repair",
    time = RoutineTime.EVENING,
    date = System.currentTimeMillis(),
    biologicalObjective = "Deep Hydration & Cell Turnover",
    contextFactors = listOf("Low Sleep Recovery"),
    steps = listOf(
        RoutineStep(id = "s5", title = "Oil Cleanser", description = "Remove makeup", isCompleted = true, layeringOrder = 1, category = CosmeticCategory.OTHER),
        RoutineStep(id = "s6", title = "Water Cleanser", description = "Cleanse pores", isCompleted = false, layeringOrder = 2, category = CosmeticCategory.OTHER),
        RoutineStep(id = "s7", title = "Retinol 0.5%", description = "Cell turnover", isCompleted = false, layeringOrder = 3, minWaitMinutes = 15, category = CosmeticCategory.OTHER),
        RoutineStep(id = "s8", title = "Night Cream", description = "Seal barrier", isCompleted = false, layeringOrder = 4, category = CosmeticCategory.OTHER)
    )
)

// Updated to leverage the new professional tracking fields
private fun getMockCosmetics(): List<CosmeticItem> {
    val now = System.currentTimeMillis()
    val monthInMillis = 30L * 24 * 60 * 60 * 1000

    return listOf(
        CosmeticItem(
            id = 1L,
            name = "Ruby Woo",
            brand = "MAC",
            category = CosmeticCategory.LIPSTICK,
            colorHex = "#E0115F",
            price = 23.00,
            usageCount = 45, // CPU = ~$0.51
            openedDate = now - (6 * monthInMillis),
            paoMonths = 12
        ),
        CosmeticItem(
            id = 2L,
            name = "Luminous Silk",
            brand = "Armani",
            category = CosmeticCategory.FOUNDATION,
            colorHex = "#FAD6A5",
            price = 69.00,
            usageCount = 120, // CPU = ~$0.57
            openedDate = now - (11 * monthInMillis), // Expiring soon warning!
            paoMonths = 12
        ),
        CosmeticItem(
            id = 3L,
            name = "Awaiting Analysis...",
            brand = "UNKNOWN",
            category = CosmeticCategory.AI_PENDING,
            colorHex = null
        ),
        CosmeticItem(
            id = 4L,
            name = "Orgasm Blush",
            brand = "NARS",
            category = CosmeticCategory.BLUSH,
            colorHex = "#FFC0CB",
            price = 32.00,
            usageCount = 10,
            openedDate = now - (2 * monthInMillis),
            paoMonths = 24
        )
    )
}

private fun getMockUiState(isDaytime: Boolean) = HomeUiState(
    isDaytime = isDaytime,
    fashionProfile = getMockFashionProfile(),
    morningRoutine = getMockMorningRoutine(),
    eveningRoutine = getMockEveningRoutine(),
    totalCosmetics = 142,
    popularCosmetics = getMockCosmetics(),
    totalClothing = 48,
    popularClothing = emptyList()
)

// --- EXPRESSIVE PREVIEWS ---

@Preview(
    name = "1. Full Screen - AM (Energetic)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun ExpressiveHomeScreenTimed_DaytimePreview() {
    MaterialTheme {
        HomeScreenTimed(
            uiState = getMockUiState(isDaytime = true),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(
    name = "2. Full Screen - PM (Restorative)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun ExpressiveHomeScreenTimed_EveningPreview() {
    MaterialTheme {
        HomeScreenTimed(
            uiState = getMockUiState(isDaytime = false),
            onEvent = {},
            navTo = {}
        )
    }
}