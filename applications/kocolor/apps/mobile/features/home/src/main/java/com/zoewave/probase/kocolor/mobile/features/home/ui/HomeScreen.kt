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
    onNavigateTo: (KoColorRoute) -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        navTo = onNavigateTo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
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
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                HomeHeader(uiState.fashionProfile)
            }

            item {
                BeautyTipSection(uiState.beautyTip)
            }

            item {
                QuickActions(
                    onAnalyze = { navTo(KoColorRoute.Analyzer()) },
                    onManageCosmetics = { navTo(KoColorRoute.Cosmetics) }
                )
            }

            item {
                val routine = if (uiState.isDaytime) uiState.morningRoutine else uiState.eveningRoutine
                val title = if (uiState.isDaytime) "morning beautiful routine" else "Evening Ritual"
                val icon = if (uiState.isDaytime) Icons.Default.LightMode else Icons.Default.Nightlight
                
                if (routine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionTitle(title = title, icon = icon)
                        RoutineSummaryCard(
                            routine = routine,
                            onClick = { navTo(KoColorRoute.Routines) }
                        )
                    }
                }
            }

            if (uiState.popularCosmetics.isNotEmpty()) {
                item {
                    SectionTitle(title = "Cosmetics", icon = Icons.Default.Brush)
                }
                item {
                    InventoryCard(
                        items = uiState.popularCosmetics.map { InventoryPreviewItem(it.name, it.colorHex) },
                        onManage = { navTo(KoColorRoute.Cosmetics) }
                    )
                }
            }

            if (uiState.popularClothing.isNotEmpty()) {
                item {
                    SectionTitle(title = "Wardrobe", icon = Icons.Default.Checkroom)
                }
                item {
                    InventoryCard(
                        items = uiState.popularClothing.map { InventoryPreviewItem(it.name, it.colorHex) },
                        onManage = { navTo(KoColorRoute.Wardrobe) }
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun HomeHeader(profile: FashionProfile?) {
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
                    text = if (profile == null) "Welcome!" else "Hello, Beautiful",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (profile == null) {
                    Text("Unlock your best look with our AI color analysis.")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(profile.seasonalType.name, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${profile.undertone} Undertone",
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
fun BeautyTipSection(tip: String) {
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
            Text(text = tip, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun QuickActions(onAnalyze: () -> Unit, onManageCosmetics: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onAnalyze,
            modifier = Modifier.weight(1f).height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze", style = MaterialTheme.typography.titleMedium)
        }
        
        OutlinedButton(
            onClick = onManageCosmetics,
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
    routine: BeautyRoutine,
    onClick: () -> Unit
) {
    val completedCount = routine.steps.count { it.isCompleted }
    val totalCount = routine.steps.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                text = "Next: " + (routine.steps.find { !it.isCompleted }?.title ?: "All done!"),
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
fun InventoryCard(items: List<InventoryPreviewItem>, onManage: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onManage
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    InventoryMiniItem(item = item, modifier = Modifier.weight(1f))
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
fun InventoryMiniItem(item: InventoryPreviewItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (item.colorHex != null) {
                        try { Color(android.graphics.Color.parseColor(item.colorHex)) } 
                        catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (item.colorHex == null) {
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
            text = item.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionTitle(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview_NoProfile() {
    MaterialTheme {
        HomeScreen(
            uiState = HomeUiState(),
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
            navTo = {}
        )
    }
}
