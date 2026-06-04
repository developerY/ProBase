package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.*
import com.zoewave.probase.kocolor.model.*

data class CosmeticDetailUiState(
    val item: CosmeticItem? = null,
    val usageFrequencyPerWeek: Double = 3.5,
    val estimatedDaysRemaining: Int? = 45,
    val colorCompatibility: List<String> = listOf("#FBF8F5", "#E6A68A", "#2C2420"), 
    val bioSyncMessage: String? = "✨ High Synergy Today: Your hydration markers are low (0.0L); this Hyaluronic Acid will compensate.",
    val uvIndex: Double = 0.0
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.applications_kocolor_features_cosmetics_atelier), 
                        style = MaterialTheme.typography.titleLarge, 
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = atelierBrown
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        onEvent(CosmeticsEvent.StartEditing(item))
                        navTo(KoColorRoute.CosmeticEdit(item.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_edit), tint = Color.Gray)
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
                                stringResource(R.string.applications_kocolor_features_cosmetics_active_fda_recall),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Text(
                                stringResource(R.string.applications_kocolor_features_cosmetics_fda_recall_status_format, status),
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
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, fdaStatusColor.copy(alpha = 0.5f))
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
                                text = stringResource(R.string.applications_kocolor_features_cosmetics_fda_label),
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
                                text = if ((item.fillLevel ?: 1.0) > 0.1) stringResource(R.string.applications_kocolor_features_cosmetics_in_stock) else stringResource(R.string.applications_kocolor_features_cosmetics_low_stock),
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
                            text = stringResource(R.string.applications_kocolor_features_cosmetics_assigned_to_format, placement),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = atelierBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.applications_kocolor_features_cosmetics_sku_launched_format, item.batchCode ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available), "Q3 2023"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // 2. Product Image Section
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)),
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
                title = stringResource(R.string.applications_kocolor_features_cosmetics_value_analysis),
                isExpanded = expandedStates["Value Analysis"] == true,
                onToggle = { expandedStates["Value Analysis"] = it }
            ) {
                ValueAnalysisSection(item)
            }

            item.colorHex?.let { colorHex ->
                AtelierExpandableSection(
                    title = stringResource(R.string.applications_kocolor_features_cosmetics_color_hue_map),
                    isExpanded = expandedStates["Color Hue Map"] == true,
                    onToggle = { expandedStates["Color Hue Map"] = it }
                ) {
                    ColorHueMapSection(colorHex, item.shadeName, uiState.colorCompatibility)
                }
            }
            
            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_clinical_safety),
                isExpanded = expandedStates["Clinical Safety"] == true,
                onToggle = { expandedStates["Clinical Safety"] = it }
            ) {
                ClinicalSafetySection(item)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_ingredient_analysis),
                isExpanded = expandedStates["Ingredient Analysis"] == true,
                onToggle = { expandedStates["Ingredient Analysis"] = it }
            ) {
                IngredientAnalysisSection(item, uiState.bioSyncMessage)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_sustainability_eco_impact),
                isExpanded = expandedStates["Sustainability"] == true,
                onToggle = { expandedStates["Sustainability"] = it }
            ) {
                SustainabilitySection(item)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_application_guide),
                isExpanded = expandedStates["Application Guide"] == true,
                onToggle = { expandedStates["Application Guide"] = it }
            ) {
                ApplicationGuideSection(item, atelierBrown)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_professional_facets),
                isExpanded = expandedStates["Professional Facets"] == true,
                onToggle = { expandedStates["Professional Facets"] = it }
            ) {
                ProfessionalFacetsSection(item)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_product_lifecycle),
                isExpanded = expandedStates["Product Lifecycle"] == true,
                onToggle = { expandedStates["Product Lifecycle"] = it }
            ) {
                ProductDatesSection(item)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_usage_stock),
                isExpanded = expandedStates["Usage & Stock"] == true,
                onToggle = { expandedStates["Usage & Stock"] = it }
            ) {
                UsageStockSection(item, uiState)
            }

            AtelierExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_coordination),
                isExpanded = expandedStates["Coordination"] == true,
                onToggle = { expandedStates["Coordination"] = it }
            ) {
                CoordinationSection(uvIndex = uiState.uvIndex)
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
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_log_usage), fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = { /* Finish */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
                ) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_mark_finished), color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
