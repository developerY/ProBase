package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import java.text.NumberFormat
import java.util.Locale

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

@Preview(showBackground = true)
@Composable
private fun WardrobeDetailScreenPreview() {
    MaterialTheme {
        WardrobeDetailScreen(
            uiState = 1L to WardrobeUiState(
                items = listOf(
                    ClothingItem(id = 1L, name = "Silk Blouse", category = com.zoewave.probase.kocolor.model.ClothingCategory.TOPS, price = 120.0)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeDetailScreen(
    uiState: Pair<Long, WardrobeUiState>,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val itemId = uiState.first
    val state = uiState.second
    val item = remember(state.items, itemId) {
        state.items.find { it.id == itemId }
    } ?: return

    val itemColor = item.dominantHex?.let { parseColor(it) } 
        ?: item.colorHex?.let { parseColor(it) } 
        ?: MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Garment Details", style = MaterialTheme.typography.labelLarge) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onEvent(WardrobeEvent.UpdateDraft(item))
                        navTo(KoColorRoute.WardrobeEdit(item.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                if (item.imageUrl != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(itemColor))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(itemColor))
                }

                // Scrim for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 600f
                            )
                        )
                )
                
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Text(
                        text = (item.brand ?: "KOCOLOR").uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
                        Text(text = item.category.name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Metrics
            Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                MetricItem(
                    uiState = Triple("INVESTMENT", item.price?.let { currencyFormatter.format(it) } ?: "---", Icons.Default.Payments),
                    onEvent = {},
                    navTo = {},
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    uiState = Triple("SIZE", item.size ?: "OS", Icons.Default.Straighten),
                    onEvent = {},
                    navTo = {},
                    modifier = Modifier.weight(1f)
                )
            }

            // Detail Content
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                
                // Color Blueprint
                Column {
                    SectionHeader(uiState = "Color Blueprint", onEvent = {}, navTo = {})
                    Spacer(Modifier.height(16.dp))
                    ColorAnalysisSection(uiState = item, onEvent = {}, navTo = {})
                }

                Column {
                    SectionHeader(uiState = "Composition", onEvent = {}, navTo = {})
                    DetailRow(uiState = "Material" to (item.material ?: "Unknown"), onEvent = {}, navTo = {})
                }
                Column {
                    SectionHeader(uiState = "Archive Notes", onEvent = {}, navTo = {})
                    ProInsightCard(uiState = item.notes to Icons.AutoMirrored.Filled.MenuBook, onEvent = {}, navTo = {})
                }
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    MaterialTheme {
        SectionHeader(uiState = "Composition", onEvent = {}, navTo = {})
    }
}

@Composable
private fun SectionHeader(uiState: String, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    Text(text = uiState.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
}

@Preview(showBackground = true)
@Composable
private fun MetricItemPreview() {
    MaterialTheme {
        MetricItem(uiState = Triple("INVESTMENT", "$100", Icons.Default.Payments), onEvent = {}, navTo = {})
    }
}

@Composable
private fun MetricItem(uiState: Triple<String, String, ImageVector>, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit, modifier: Modifier = Modifier) {
    val (label, value, icon) = uiState
    Column(modifier = modifier) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailRowPreview() {
    MaterialTheme {
        DetailRow(uiState = "Material" to "Silk", onEvent = {}, navTo = {})
    }
}

@Composable
private fun DetailRow(uiState: Pair<String, String>, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    val (label, value) = uiState
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ColorAnalysisSection(
    uiState: ClothingItem, 
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState
    val colors = remember(item) {
        mutableListOf<String>().apply {
            item.dominantHex?.let { add(it) }
            item.vibrantHex?.let { add(it) }
            item.mutedHex?.let { add(it) }
            addAll(item.paletteHexes)
            
            // Fallback to manual colorHex if no engine data yet
            if (isEmpty()) {
                item.colorHex?.let { add(it) }
            }
        }.distinct()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.take(5).forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(parseColor(hex))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
        }

        if (item.seasonalPalette != null || item.colorTemperature != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Temperature", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.colorTemperature ?: "Neutral", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Seasonal Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.seasonalPalette ?: "Universal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProInsightCardPreview() {
    MaterialTheme {
        ProInsightCard(uiState = "Notes" to Icons.AutoMirrored.Filled.MenuBook, onEvent = {}, navTo = {})
    }
}

@Composable
private fun ProInsightCard(uiState: Pair<String?, ImageVector>, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    val content = uiState.first
    val icon = uiState.second
    val isAvailable = !content.isNullOrBlank()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isAvailable) 0.5f else 0.1f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(text = content ?: "No notes provided.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAvailable) 0.8f else 0.2f), lineHeight = 22.sp)
        }
    }
}
