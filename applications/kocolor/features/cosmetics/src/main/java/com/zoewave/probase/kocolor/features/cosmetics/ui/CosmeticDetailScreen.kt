package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import com.zoewave.probase.kocolor.model.*
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark

data class CosmeticDetailUiState(
    val item: CosmeticItem? = null
)

@Preview(showBackground = true, name = "Populated")
@Composable
private fun CosmeticDetailScreenPreview() {
    MaterialTheme {
        CosmeticDetailScreen(
            uiState = CosmeticDetailUiState(
                item = CosmeticItem(
                    id = 1L,
                    name = "Cool Ivory Foundation",
                    brand = "KoColor",
                    macroCategory = MacroCategory.COMPLEXION,
                    microCategory = MicroCategory.FOUNDATION,
                    formulation = Formulation.LIQUID,
                    chemistryBase = ChemistryBase.WATER,
                    finish = Finish.NATURAL,
                    coverage = Coverage.MEDIUM,
                    price = 42.0,
                    volume = "30ml",
                    amountRemaining = 5.0,
                    amountPerUse = 0.35,
                    usageCount = 120,
                    isOpened = true,
                    openedDate = System.currentTimeMillis() - (100L * 24 * 60 * 60 * 1000)
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true, name = "Not Tracked")
@Composable
private fun CosmeticDetailScreen_NotTracked_Preview() {
    MaterialTheme {
        CosmeticDetailScreen(
            uiState = CosmeticDetailUiState(
                item = CosmeticItem(
                    id = 2L,
                    name = "Silk Primer",
                    brand = "KoColor",
                    macroCategory = MacroCategory.PREP,
                    microCategory = MicroCategory.PRIMER,
                    price = null,
                    volume = "30ml",
                    amountRemaining = null,
                    amountPerUse = null,
                    usageCount = 0
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
    uiState: CosmeticDetailUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState.item ?: return
    val bgColor = item.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    
    var showProductInfo by remember { mutableStateOf(true) }
    var showProfessionalFacets by remember { mutableStateOf(true) }
    var showStockHealth by remember { mutableStateOf(false) }
    var showValueAnalysis by remember { mutableStateOf(false) }
    var showApplicationGuide by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Product Details", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Serif) },
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
            // 1. Hero Visual
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                if (item.imageUrl != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(bgColor))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(bgColor))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 500f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        text = item.brand.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    if (!item.shadeName.isNullOrBlank()) {
                        Text(
                            text = item.shadeName!!,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 2. Expandable Body
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricItem(uiState = Triple("Micro", item.microCategory.displayName, Icons.Default.Category), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                    MetricItem(uiState = Triple("Price", item.price?.let { "$%.2f".format(it) } ?: "N/A", Icons.Default.Payments), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                    
                    val unit = item.volume?.filter { it.isLetter() } ?: "ml"
                    val usage = item.amountPerUse ?: item.microCategory.typicalAmountPerUse
                    MetricItem(uiState = Triple("Usage", "%.2f %s".format(usage, unit), Icons.Default.Opacity), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                    
                    MetricItem(uiState = Triple("Uses", item.usageCount.toString(), Icons.Default.History), onEvent = {}, navTo = {}, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // SECTION 1: PRODUCT METADATA
                ExpandableSection(
                    title = "Product Metadata",
                    isExpanded = showProductInfo,
                    onToggle = { showProductInfo = !showProductInfo }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow(uiState = Triple("Macro Category", item.macroCategory.displayName, Color.Unspecified), onEvent = {}, navTo = {})
                        DetailRow(uiState = Triple("Batch Code / SKU", item.batchCode ?: "Not Set", Color.Unspecified), onEvent = {}, navTo = {})
                        DetailRow(uiState = Triple("Status", if (item.isOpened) "Opened" else "New / Sealed", Color.Unspecified), onEvent = {}, navTo = {})
                        DetailRow(uiState = Triple("Container Volume", item.volume ?: "Unknown", Color.Unspecified), onEvent = {}, navTo = {})
                        
                        item.estimatedExpiry?.let { expiry ->
                            val daysLeft = ((expiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                            DetailRow(uiState = Triple("Estimated Expiry", if (daysLeft > 0) "$daysLeft days left" else "Expired", if (daysLeft < 30) Color.Red else MaterialTheme.colorScheme.onSurface), onEvent = {}, navTo = {})
                        }
                    }
                }

                // SECTION 2: PROFESSIONAL FACETS
                ExpandableSection(
                    title = "Professional Facets",
                    isExpanded = showProfessionalFacets,
                    onToggle = { showProfessionalFacets = !showProfessionalFacets }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow(uiState = Triple("Formulation", item.formulation.name.lowercase().replaceFirstChar { it.uppercase() }, Color.Unspecified), onEvent = {}, navTo = {})
                        DetailRow(uiState = Triple("Chemistry Base", item.chemistryBase.name.lowercase().replaceFirstChar { it.uppercase() }, Color.Unspecified), onEvent = {}, navTo = {})
                        DetailRow(uiState = Triple("Finish", item.finish.name.lowercase().replaceFirstChar { it.uppercase() }, Color.Unspecified), onEvent = {}, navTo = {})
                        DetailRow(uiState = Triple("Coverage", item.coverage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }, Color.Unspecified), onEvent = {}, navTo = {})
                    }
                }

                // SECTION 3: STOCK HEALTH
                ExpandableSection(
                    title = "Stock Health",
                    isExpanded = showStockHealth,
                    onToggle = { showStockHealth = !showStockHealth }
                ) {
                    val isTrackingActive = item.amountRemaining != null
                    val fillLevel = item.fillLevel ?: 1.0
                    val statusColor = if (!isTrackingActive) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    } else {
                        when {
                            fillLevel > 0.5 -> Color(0xFF4CAF50) // Green
                            fillLevel > 0.2 -> Color(0xFFFFC107) // Yellow
                            else -> Color(0xFFF44336) // Red
                        }
                    }
                    val unit = item.volume?.filter { it.isLetter() } ?: "units"
                    
                    Column(
                        modifier = Modifier.alpha(if (isTrackingActive) 1f else 0.5f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("AMOUNT REMAINING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                Text(
                                    text = item.amountRemaining?.let { "%.1f %s".format(it, unit) } ?: "Not Tracked",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "%.2f %s per use".format(item.amountPerUse ?: item.microCategory.typicalAmountPerUse, unit),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                                CircularProgressIndicator(
                                    progress = { fillLevel.toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    color = statusColor,
                                    trackColor = statusColor.copy(alpha = 0.1f),
                                    strokeWidth = 6.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                Text(
                                    text = if (isTrackingActive) "${(fillLevel * 100).toInt()}%" else "---",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                        
                        LinearProgressIndicator(
                            progress = { fillLevel.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.1f)
                        )
                    }
                }

                // SECTION 4: VALUE ANALYSIS
                ExpandableSection(
                    title = "Value Analysis",
                    isExpanded = showValueAnalysis,
                    onToggle = { showValueAnalysis = !showValueAnalysis }
                ) {
                    if (item.costPerUse != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Actual Cost Per Use", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text("$%.2f".format(item.costPerUse), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                                }
                                Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            }
                        }
                    } else {
                        Text("Complete more usage cycles to calculate performance data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // SECTION 5: APPLICATION GUIDE
                ExpandableSection(
                    title = "Application Guide",
                    isExpanded = showApplicationGuide,
                    onToggle = { showApplicationGuide = !showApplicationGuide }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = { onEvent(CosmeticsEvent.UseItem(item.id)) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Log Today's Use", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                OutlinedButton(
                    onClick = { /* Add to Routine */ },
                    modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text("Add to Beauty Routine", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun MetricItem(
    uiState: Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>, 
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = uiState.first
    val value = uiState.second
    val icon = uiState.third
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
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
                text = content ?: "No $title details provided.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAvailable) 0.8f else 0.2f),
                lineHeight = 22.sp
            )
        }
    }
}
