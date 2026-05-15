package com.zoewave.probase.kocolor.mobile.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

            if (uiState.popularCosmetics.isNotEmpty()) {
                item {
                    SectionTitle(
                        uiState = "Cosmetics" to Icons.Default.Brush,
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    InventoryCard(
                        uiState = uiState.popularCosmetics.map { InventoryPreviewItem(it.name, it.colorHex) },
                        onEvent = {},
                        navTo = { navTo(KoColorRoute.Cosmetics) }
                    )
                }
            }

            if (uiState.popularClothing.isNotEmpty()) {
                item {
                    SectionTitle(
                        uiState = "Wardrobe" to Icons.Default.Checkroom,
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    InventoryCard(
                        uiState = uiState.popularClothing.map { InventoryPreviewItem(it.name, it.colorHex) },
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

data class InventoryPreviewItem(
    val name: String,
    val colorHex: String?
)

@Composable
fun InventoryCard(
    uiState: List<InventoryPreviewItem>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = { navTo(KoColorRoute.Back) } // Using Back as a generic onClick trigger for now if needed, or specific route
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.forEach { item ->
                    InventoryMiniItem(
                        uiState = item,
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Manage inventory",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun InventoryMiniItem(
    uiState: InventoryPreviewItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp), // Replaced weight(1f) to stay within signature only
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (uiState.colorHex != null) {
                        try { Color(android.graphics.Color.parseColor(uiState.colorHex)) } 
                        catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.colorHex == null) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = uiState.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionTitle(
    uiState: Pair<String, ImageVector>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
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
private fun InventoryMiniItemPreview() {
    MaterialTheme {
        InventoryMiniItem(uiState = InventoryPreviewItem("Lipstick", "#FF0000"), onEvent = {}, navTo = {})
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
