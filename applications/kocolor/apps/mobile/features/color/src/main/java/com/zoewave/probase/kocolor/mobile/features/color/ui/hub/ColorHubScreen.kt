package com.zoewave.probase.kocolor.mobile.features.color.ui.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorSignature
import com.zoewave.probase.kocolor.features.colors.domain.model.SourceType
import com.zoewave.probase.kocolor.model.KoColorRoute
import android.graphics.Color as AndroidColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorHubScreen(
    uiState: ColorHubUiState,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var selectedGroup by remember { mutableStateOf<Pair<String, List<ColorSignature>>?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Color Hub", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Chromatic DNA (The Inventory Visualization)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chromatic DNA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    if (selectedGroup != null) {
                        IconButton(
                            onClick = { selectedGroup = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Collapse spectrum",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                ChromaticDnaBar(
                    colors = uiState.inventoryColors,
                    selectedGroup = selectedGroup,
                    onGroupSelected = { selectedGroup = it },
                    navTo = navTo
                )
            }

            // 2. Palette Gap Analysis
            item {
                Text("Palette Gaps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Detected based on your ${uiState.userSeason.name} profile", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                
                if (uiState.paletteGaps.isEmpty()) {
                    Text("Your inventory is perfectly balanced!", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.paletteGaps) { hex ->
                            GapIndicator(hex = hex)
                        }
                    }
                }
            }
            
            // 3. Recommended Additions (Simulated)
            item {
                Text("Suggested Harmonies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Text(
                        "Try adding more deep jewel tones to your wardrobe to complement your current collection of cool neutrals.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            item { Spacer(Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun ChromaticDnaBar(
    colors: List<ColorSignature>,
    selectedGroup: Pair<String, List<ColorSignature>>?,
    onGroupSelected: (Pair<String, List<ColorSignature>>?) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val colorGroups = remember(colors) {
        colors
            .filter { it.hex.isNotBlank() }
            .groupBy { it.hex }
            .toList()
            .sortedWith(compareBy(
                { (hex, _) ->
                    val hsv = FloatArray(3)
                    try {
                        AndroidColor.colorToHSV(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
                        if (hsv[1] < 0.1f) 1 else 0 
                    } catch (e: Exception) { 1 }
                },
                { (hex, _) ->
                    val hsv = FloatArray(3)
                    try {
                        AndroidColor.colorToHSV(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
                        val hue = hsv[0]
                        if (hue > 330) hue - 360 else hue
                    } catch (e: Exception) { 0f }
                },
                { (hex, _) ->
                    val hsv = FloatArray(3)
                    try {
                        AndroidColor.colorToHSV(AndroidColor.parseColor(if (hex.startsWith("#")) hex else "#$hex"), hsv)
                        hsv[2] 
                    } catch (e: Exception) { 0f }
                }
            ))
    }

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // The Spectrum Bar (Harmonica Scroll Effect)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val totalGroups = colorGroups.size
            val containerWidth = maxWidth
            
            // Calculate unselected width to fill exactly the container if possible, 
            // but keep a minimum for visibility.
            val baseItemWidth = if (totalGroups > 0) (containerWidth / totalGroups).coerceAtLeast(4.dp) else 0.dp

            LazyRow(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.Start
            ) {
                items(colorGroups.size) { index ->
                    val group = colorGroups[index]
                    val (hex, _) = group
                    val isSelected = selectedGroup?.first == hex
                    
                    // harmonica expansion effect
                    val animatedWidth by animateDpAsState(
                        targetValue = when {
                            selectedGroup == null -> baseItemWidth
                            isSelected -> 100.dp
                            else -> 12.dp // Shrink neighbors
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "width"
                    )

                    Box(
                        modifier = Modifier
                            .width(animatedWidth)
                            .fillMaxHeight()
                            .background(parseColor(hex))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent
                            )
                            .clickable { 
                                if (isSelected) {
                                    onGroupSelected(null)
                                } else {
                                    onGroupSelected(group)
                                    scope.launch {
                                        // Center the clicked item
                                        lazyListState.animateScrollToItem(index)
                                    }
                                }
                            }
                    )
                }
            }
        }

        // 🔍 Selection Details
        selectedGroup?.let { (hex, items) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "${items.size} ${if (items.size == 1) "Item" else "Items"} in this shade",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items.forEach { sig ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                val route = when (sig.sourceType) {
                                    SourceType.WARDROBE -> KoColorRoute.WardrobeDetail(sig.sourceId)
                                    SourceType.VANITY -> KoColorRoute.CosmeticDetail(sig.sourceId)
                                }
                                navTo(route)
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sig.name ?: "Unnamed Item", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = when (sig.sourceType) {
                                    SourceType.WARDROBE -> "Wardrobe"
                                    SourceType.VANITY -> "Vanity"
                                }, 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).rotate(180f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GapIndicator(hex: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(parseColor(hex))
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.height(8.dp))
        Text("Missing", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}
