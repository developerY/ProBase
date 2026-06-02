package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.ChemistryBase
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.Coverage
import com.zoewave.probase.kocolor.model.Finish
import com.zoewave.probase.kocolor.model.Formulation
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.MacroCategory
import com.zoewave.probase.kocolor.model.MicroCategory

data class CosmeticDetailUiState(
    val item: CosmeticItem? = null,
    // Usage Insights
    val usageFrequencyPerWeek: Double = 3.5,
    val estimatedDaysRemaining: Int? = 45,
    val colorCompatibility: List<String> = listOf("#FBF8F5", "#E6A68A", "#2C2420"), // Seasonal coordination
    val bioSyncMessage: String? = "✨ High Synergy Today: Your hydration markers are low (0.0L); this Hyaluronic Acid will compensate."
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticDetailScreen(
    uiState: CosmeticDetailUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val item = uiState.item ?: return
    val atelierBrown = Color(0xFF8B5E3C)
    val statusColor = if ((item.fillLevel ?: 1.0) > 0.1) Color(0xFF4CAF50) else Color.Gray
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onEvent(CosmeticsEvent.DeleteItem(item.id))
                        showDeleteConfirmation = false
                        navTo(KoColorRoute.Back)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to permanently remove ${item.name} from your collection?") },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Atelier", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = atelierBrown
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            )
        }
    ) { padding ->
        val expandedStates = remember { 
            mutableStateMapOf<String, Boolean>().apply {
                put("Clinical Safety", true)
                put("Ingredient Analysis", false)
                put("Sustainability", false)
                put("Application Guide", false)
                put("Professional Facets", false)
                put("Product Lifecycle", false)
                put("Usage & Stock", true)
                put("Coordination", false)
            }
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            // 0. Recall Banner (High Priority)
            item.fdaRecallStatus?.let { status ->
                Surface(
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "ACTIVE FDA RECALL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "Status: $status. Discontinue use immediately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 1. Header Information
            Column(modifier = Modifier.padding(24.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.microCategory.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    // FDA Clinical Safety Badge (Green/Red/Gray Indicator)
                    val fdaStatusColor = when {
                        !item.isFdaChecked -> Color.Gray // Not checked yet
                        item.fdaRecallStatus != null -> Color(0xFFD32F2F) // Red for Recall
                        item.fdaAdverseEventCount > 10 -> Color(0xFFFF9800) // Orange for High Events
                        else -> Color(0xFF4CAF50) // Green for Checked & Safe
                    }
                    
                    Surface(
                        color = fdaStatusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, fdaStatusColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (item.fdaRecallStatus != null) Icons.Default.Warning else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = fdaStatusColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "FDA",
                                style = MaterialTheme.typography.labelSmall,
                                color = fdaStatusColor,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(12.dp), tint = statusColor)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if ((item.fillLevel ?: 1.0) > 0.1) "In Stock" else "Low Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2420)
                )

                item.ritualPlacement?.let { placement ->
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = atelierBrown.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Assigned to: $placement",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = atelierBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "SKU: ${item.batchCode ?: "N/A"} • Launched: Q3 2023",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { 
                            onEvent(CosmeticsEvent.StartEditing(item))
                            navTo(KoColorRoute.CosmeticEdit(item.id))
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("Edit Product", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { /* Export */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = atelierBrown)
                    ) {
                        Text("Export Report", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Product Image Section
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
                color = Color(0xFFFBF8F5)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(24.dp)) {
                    if (item.imageUrl != null) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(Icons.Default.Image, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Expandable Sections
            AtelierExpandableSection(
                title = "Value Analysis",
                isExpanded = expandedStates["Value Analysis"] == true,
                onToggle = { expandedStates["Value Analysis"] = it }
            ) {
                ValueAnalysisSection(item)
            }

            item.colorHex?.let { colorHex ->
                AtelierExpandableSection(
                    title = "Color Hue Map",
                    isExpanded = expandedStates["Color Hue Map"] == true,
                    onToggle = { expandedStates["Color Hue Map"] = it }
                ) {
                    ColorHueMapSection(colorHex, item.shadeName, uiState.colorCompatibility)
                }
            }
            
            // CLINICAL SAFETY (High Priority, Expanded by default)
            AtelierExpandableSection(
                title = "Clinical Safety",
                isExpanded = expandedStates["Clinical Safety"] == true,
                onToggle = { expandedStates["Clinical Safety"] = it }
            ) {
                ClinicalSafetySection(item)
            }

            AtelierExpandableSection(
                title = "Ingredient Analysis",
                isExpanded = expandedStates["Ingredient Analysis"] == true,
                onToggle = { expandedStates["Ingredient Analysis"] = it }
            ) {
                IngredientAnalysisSection(item, uiState.bioSyncMessage)
            }

            AtelierExpandableSection(
                title = "Sustainability & Eco-Impact",
                isExpanded = expandedStates["Sustainability"] == true,
                onToggle = { expandedStates["Sustainability"] = it }
            ) {
                SustainabilitySection(item)
            }

            AtelierExpandableSection(
                title = "Application Guide",
                isExpanded = expandedStates["Application Guide"] == true,
                onToggle = { expandedStates["Application Guide"] = it }
            ) {
                ApplicationGuideSection(item, atelierBrown)
            }

            AtelierExpandableSection(
                title = "Professional Facets",
                isExpanded = expandedStates["Professional Facets"] == true,
                onToggle = { expandedStates["Professional Facets"] = it }
            ) {
                ProfessionalFacetsSection(item)
            }

            AtelierExpandableSection(
                title = "Product Lifecycle",
                isExpanded = expandedStates["Product Lifecycle"] == true,
                onToggle = { expandedStates["Product Lifecycle"] = it }
            ) {
                ProductDatesSection(item)
            }

            AtelierExpandableSection(
                title = "Usage & Stock",
                isExpanded = expandedStates["Usage & Stock"] == true,
                onToggle = { expandedStates["Usage & Stock"] = it }
            ) {
                UsageStockSection(item, uiState)
            }

            AtelierExpandableSection(
                title = "Coordination",
                isExpanded = expandedStates["Coordination"] == true,
                onToggle = { expandedStates["Coordination"] = it }
            ) {
                CoordinationSection()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onEvent(CosmeticsEvent.UseItem(item.id)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = atelierBrown)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Today's Usage", fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = { /* Finish */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
                ) {
                    Text("Mark as Finished", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AtelierExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFF0F0F0))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isExpanded) }
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Black,
                color = if (isExpanded) Color(0xFF8B5E3C) else Color(0xFF2C2420)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ApplicationGuideSection(item: CosmeticItem, atelierBrown: Color) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = item.instructions ?: "No manufacturer instructions provided.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 22.sp
        )
        item.notes?.let { notes ->
            if (notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFFBF8F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PERSONAL NOTES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = atelierBrown)
                        Spacer(Modifier.height(8.dp))
                        Text(notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicalSafetySection(item: CosmeticItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when {
            !item.isFdaChecked -> {
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text("FDA Safety check pending...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            item.fdaRecallStatus != null -> {
                WarningBanner(
                    title = "ACTIVE FDA RECALL",
                    subtitle = "This product has been flagged by the FDA: ${item.fdaRecallStatus}. Discontinue use.",
                    icon = Icons.Default.Warning
                )
            }
            item.fdaAdverseEventCount > 0 -> {
                WarningBanner(
                    title = "${item.fdaAdverseEventCount} Adverse Events Reported",
                    subtitle = "Reported reactions: ${item.fdaTopReactions.take(3).joinToString(", ")}",
                    icon = Icons.Default.Warning
                )
            }
            else -> {
                Surface(
                    color = Color(0xFFF0F7F0),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(20.dp), tint = Color(0xFF2E7D32))
                        Spacer(Modifier.width(12.dp))
                        Text("No adverse events reported to the FDA.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        if (item.fdaActiveIngredients.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("OTC ACTIVE INGREDIENTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
            item.fdaActiveIngredients.forEach { ingredient ->
                DetailMetricRow("Active", ingredient)
            }
        }

        if (item.fdaClinicalWarnings.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("FDA CLINICAL WARNINGS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
            item.fdaClinicalWarnings.take(2).forEach { warning ->
                Text(
                    text = "• $warning",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun WarningBanner(title: String, subtitle: String, icon: ImageVector) {
    Surface(
        color = Color(0xFFFFF0F0),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color(0xFFD32F2F))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
private fun IngredientAnalysisSection(item: CosmeticItem, bioSyncMessage: String?) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailMetricRow("Hero Ingredient", item.heroIngredient ?: "Analyzing...")
        
        bioSyncMessage?.let { msg ->
            Surface(
                color = Color(0xFFF0F7F0),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        } ?: DetailMetricRow("Skin Compatibility", item.skinCompatibility ?: "Universal")

        DetailMetricRow("Fragrance", if (item.containsFragrance == true) "Contains Fragrance" else "None / Unscented")
        
        if (item.allergens.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("ALLERGEN ALERTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFFD32F2F))
            Surface(
                color = Color(0xFFFFF0F0),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp), tint = Color(0xFFD32F2F))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Contains: ${item.allergens.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                Text("Free of common allergens", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            }
        }

        if (item.ingredients.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("FULL INGREDIENTS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
            Surface(
                color = Color(0xFFFBF8F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.ingredients.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SustainabilitySection(item: CosmeticItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item.ecoScore?.let { score ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when(score) {
                        "A" -> Color(0xFF4CAF50)
                        "B" -> Color(0xFF8BC34A)
                        "C" -> Color(0xFFFFEB3B)
                        "D" -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(score, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("ECO-SCORE: $score", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Text("Overall environmental footprint.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        DetailMetricRow("Recyclable", if (item.recyclingInstructions != null) "Yes" else "Likely")
        DetailMetricRow("Vegan", if (item.isVegan == true) "Certified" else if (item.isVegan == false) "No" else "Likely")
        DetailMetricRow("Cruelty-Free", if (item.isCrueltyFree == true) "Yes" else "Analyzing...")
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Recycling, "Recyclable", tint = Color(0xFF4CAF50))
            if (item.isVegan != false) Icon(Icons.Default.Eco, "Clean Beauty", tint = Color(0xFF4CAF50))
        }
    }
}

@Composable
private fun CoordinationSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("WORKS WELL WITH", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Silk Primer", "Radiance Mist", "Daily SPF").forEach { product ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Text(
                        text = product,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ValueAnalysisSection(item: CosmeticItem) {
    val remainingValue = (item.fillLevel ?: 1.0) * (item.price ?: 0.0)
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailMetricRow("Retail Investment", "$%.2f".format(item.price ?: 0.0))
        DetailMetricRow("Actual Cost Per Use", item.costPerUse?.let { "$%.2f".format(it) } ?: "---")
        DetailMetricRow("Remaining Value", "$%.2f".format(remainingValue), valueColor = Color(0xFF8B5E3C))
    }
}

@Composable
private fun ColorHueMapSection(colorHex: String, shadeName: String?, compatibility: List<String>) {
    val color = parseColor(colorHex)
    val hsv = remember(colorHex) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(colorHex), hsv)
        hsv
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Color Hue Map",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
            )
            
            Spacer(Modifier.width(20.dp))
            
            Column {
                Text(text = shadeName ?: "Custom Tone", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Hue: ${hsv[0].toInt()}° • Saturation: ${(hsv[1] * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Hue Spectrum
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                    )
                )
        ) {
            val bias = hsv[0] / 360f
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (bias * 320).dp) // Approximate mapping
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("COORDINATION PALETTE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            compatibility.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(parseColor(hex))
                        .border(0.5.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
private fun ProfessionalFacetsSection(item: CosmeticItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Professional Facets",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailMetricRow("Formulation", item.formulation.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailMetricRow("Chemistry Base", item.chemistryBase.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailMetricRow("Finish", item.finish.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailMetricRow("Coverage", item.coverage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
        }
    }
}

@Composable
private fun UsageStockSection(item: CosmeticItem, uiState: CosmeticDetailUiState) {
    val fillLevel = item.fillLevel ?: 1.0
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Usage & Stock Health",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("TOTAL USES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${item.usageCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("EST. DAYS LEFT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${uiState.estimatedDaysRemaining ?: "---"}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        ShareProgressBar("Stock Remaining", fillLevel)
        
        if (fillLevel < 0.2) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* Add to Shopping List */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add to Shopping List", fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Based on your average of ${uiState.usageFrequencyPerWeek} uses per week.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ProductDatesSection(item: CosmeticItem) {
    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    
    fun formatLong(timestamp: Long?): String = timestamp?.let { dateFormatter.format(java.util.Date(it)) } ?: "Not Set"

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Product Lifecycle",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailMetricRow("Added to Collection", formatLong(item.timestamp))
            DetailMetricRow("First Opened", formatLong(item.openedDate))
            DetailMetricRow("Estimated Expiry", formatLong(item.estimatedExpiry), valueColor = if ((item.estimatedExpiry ?: Long.MAX_VALUE) < System.currentTimeMillis()) Color.Red else Color.Black)
            
            // Note: CosmeticItem doesn't have a specific "last used" timestamp, 
            // but we can infer it from usageCount or future feature additions.
            // For now, using placeholders if fields are missing in model.
        }
    }
}

@Composable
private fun DetailMetricRow(label: String, value: String, valueColor: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif, color = valueColor)
    }
}

@Composable
private fun ShareProgressBar(label: String, share: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text("%.1f%%".format(share * 100), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { share.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = Color(0xFF8B5E3C),
            trackColor = Color(0xFFF0F0F0)
        )
    }
}



