package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark

@Preview(showBackground = true)
@Composable
private fun CosmeticDetailScreenPreview() {
    MaterialTheme {
        CosmeticDetailScreen(
            uiState = 1L to CosmeticsUiState(
                items = listOf(
                    CosmeticItem(
                        id = 1L,
                        name = "Foundation",
                        brand = "Luxury",
                        category = com.zoewave.probase.kocolor.model.CosmeticCategory.FOUNDATION,
                        price = 45.0
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticDetailScreen(
    uiState: Pair<Long, CosmeticsUiState>,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val itemId = uiState.first
    val item = uiState.second.items.find { it.id == itemId } ?: return
    val bgColor = item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val isDark = isColorDark(bgColor)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Product Details", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        onEvent(CosmeticsEvent.StartEditing(item))
                        navTo(KoColorRoute.CosmeticEdit(item.id))
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
            // 1. Hero Visual (Image + Color)
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
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(bgColor))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(bgColor))
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

                // Bottom Content Over Hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = item.brand.uppercase(),
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
                    if (!item.shadeName.isNullOrBlank()) {
                        Text(
                            text = item.shadeName!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 2. Details Section
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Professional Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(uiState = Triple("Category", item.category.displayName, Icons.Default.Category), onEvent = {}, navTo = {})
                    MetricItem(uiState = Triple("Price", item.price?.let { "$%.2f".format(it) } ?: "N/A", Icons.Default.Payments), onEvent = {}, navTo = {})
                    MetricItem(uiState = Triple("Uses", item.usageCount.toString(), Icons.Default.History), onEvent = {}, navTo = {})
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Cost Per Use Section (Magazine Style)
                if (item.costPerUse != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cost Per Use", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text("$%.2f".format(item.costPerUse), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                            }
                            Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Additional Metadata
                DetailRow(uiState = Triple("Batch Code / SKU", item.batchCode ?: "Not Set", Color.Unspecified), onEvent = {}, navTo = {})
                DetailRow(uiState = Triple("Status", if (item.isOpened) "Opened" else "New / Sealed", Color.Unspecified), onEvent = {}, navTo = {})
                
                item.estimatedExpiry?.let { expiry ->
                    val daysLeft = ((expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                    DetailRow(uiState = Triple("Estimated Expiry", if (daysLeft > 0) "$daysLeft days left" else "Expired", if (daysLeft < 30) Color.Red else MaterialTheme.colorScheme.onSurface), onEvent = {}, navTo = {})
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Pro Insights (Shelf Life, Usage, Notes)
                SectionHeader(uiState = "Shelf Life", onEvent = {}, navTo = {})
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val daysRemaining = item.estimatedExpiry?.let { ((it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt() }
                    ProMetricCard(
                        uiState = Quadruple(
                            "TIME REMAINING",
                            if (daysRemaining != null) "$daysRemaining Days" else null,
                            Icons.Default.HourglassEmpty,
                            daysRemaining?.let { it / 365f }
                        ),
                        onEvent = {},
                        navTo = {},
                        modifier = Modifier.weight(1.5f)
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val openedStr = item.openedDate?.let { 
                        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(it))
                    }
                    ProMetricCard(
                        uiState = Quadruple("OPENED ON", openedStr, null, null),
                        onEvent = {},
                        navTo = {},
                        modifier = Modifier.weight(1f)
                    )
                    
                    val expiryStr = item.estimatedExpiry?.let { 
                        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(it))
                    }
                    ProMetricCard(
                        uiState = Quadruple("EXPIRES BY", expiryStr, null, null),
                        onEvent = {},
                        navTo = {},
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(uiState = "Usage Details", onEvent = {}, navTo = {})
                
                ProInsightCard(
                    uiState = Triple("Instructions", item.instructions, Icons.Default.Opacity),
                    onEvent = {},
                    navTo = {}
                )

                ProInsightCard(
                    uiState = Triple("Personal Notes", item.notes, Icons.Default.StickyNote2),
                    onEvent = {},
                    navTo = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = { onEvent(CosmeticsEvent.UseItem(item.id)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Log Today's Use", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { /* Add to Routine */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Add to Beauty Routine")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionHeaderPreview() {
    MaterialTheme {
        SectionHeader(uiState = "Header", onEvent = {}, navTo = {})
    }
}

@Composable
private fun SectionHeader(uiState: String, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    Text(
        text = uiState,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Preview(showBackground = true)
@Composable
private fun ProMetricCardPreview() {
    MaterialTheme {
        ProMetricCard(
            uiState = Quadruple("Title", "Value", Icons.Default.Info, 0.5f),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun ProMetricCard(
    uiState: Quadruple<String, String?, androidx.compose.ui.graphics.vector.ImageVector?, Float?>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = uiState.first
    val value = uiState.second
    val icon = uiState.third
    val progress = uiState.fourth
    val isAvailable = value != null
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isAvailable) 0.6f else 0.3f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value ?: "Pending",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAvailable) 1f else 0.2f),
                    fontWeight = FontWeight.Black
                )
            }
            
            if (icon != null) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (progress != null) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isAvailable) 1f else 0.2f),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 4.dp
                        )
                    }
                    Icon(
                        icon, 
                        contentDescription = null, 
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isAvailable) 0.8f else 0.2f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProInsightCardPreview() {
    MaterialTheme {
        ProInsightCard(
            uiState = Triple("Title", "Content", Icons.Default.Info),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun ProInsightCard(
    uiState: Triple<String, String?, androidx.compose.ui.graphics.vector.ImageVector>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val title = uiState.first
    val content = uiState.second
    val icon = uiState.third
    val isAvailable = !content.isNullOrBlank()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp), 
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isAvailable) 1f else 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title, 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isAvailable) 1f else 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content ?: "No $title details provided by manufacturer or user.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAvailable) 0.8f else 0.2f),
                lineHeight = 22.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MetricItemPreview() {
    MaterialTheme {
        MetricItem(
            uiState = Triple("Label", "Value", Icons.Default.Info),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun MetricItem(uiState: Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    val label = uiState.first
    val value = uiState.second
    val icon = uiState.third
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailRowPreview() {
    MaterialTheme {
        DetailRow(
            uiState = Triple("Label", "Value", Color.Unspecified),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
private fun DetailRow(uiState: Triple<String, String, Color>, onEvent: (Unit) -> Unit, navTo: (KoColorRoute) -> Unit) {
    val label = uiState.first
    val value = uiState.second
    val valueColor = uiState.third
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
