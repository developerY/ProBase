package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.ChemistryBase
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Coverage
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Formulation
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.features.camera.productcapture.ui.DiscoveryStatusScreen
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.core.ui.util.isColorDark
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.core.ui.util.toHex
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.AtelierExpandableSection
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true, name = "Add Mode")
@Composable
private fun StitchProductBuilderAddPreview() {
    MaterialTheme {
        StitchProductBuilder(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode")
@Composable
private fun StitchProductBuilderEditPreview() {
    MaterialTheme {
        StitchProductBuilder(
            uiState = CosmeticsUiState(
                draftItem = CosmeticItem(
                    internalId = 1L,
                    name = "Luminous Silk Foundation",
                    brand = "Armani",
                    macroCategory = MacroCategory.COMPLEXION,
                    microCategory = MicroCategory.FOUNDATION,
                    colorHex = "#FFFFFF"
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchProductBuilder(
    uiState: CosmeticsUiState,
    modifier: Modifier = Modifier,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.draftItem
    val scrollState = rememberScrollState()
    val atelierBrown = Color(0xFF8B5E3C)
    val isEditMode = draft.internalId != 0L

    var showColorPicker by remember { mutableStateOf(false) }
    var showDatePickerTarget by remember { mutableStateOf<String?>(null) }
    var showMacroMenu by remember { mutableStateOf(false) }
    var showMicroMenu by remember { mutableStateOf(false) }
    var showFormulationMenu by remember { mutableStateOf(false) }
    var showChemistryMenu by remember { mutableStateOf(false) }
    var showFinishMenu by remember { mutableStateOf(false) }
    var showCoverageMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val expandedSections = remember { 
        mutableStateMapOf<String, Boolean>().apply {
            put("Core", true)
            put("Taxonomy", false)
            put("Safety", false)
            put("Analysis", false)
            put("Sustainability", false)
            put("Economics", false)
            put("Lifecycle", false)
        }
    }

    BackHandler {
        onEvent(CosmeticsEvent.CancelDiscovery)
        navTo(KoColorRoute.Back)
    }

    if (showDatePickerTarget != null) {
        val initialDate = when(showDatePickerTarget) {
            "OPENED" -> draft.openedDate
            "EXPIRY" -> draft.expiryDate
            else -> null
        }
        AtelierDatePicker(
            initialDate = initialDate ?: System.currentTimeMillis(),
            onDateSelected = { date ->
                val updated = when(showDatePickerTarget) {
                    "OPENED" -> draft.copy(openedDate = date, isOpened = true)
                    "EXPIRY" -> draft.copy(expiryDate = date)
                    else -> draft
                }
                onEvent(CosmeticsEvent.UpdateDraft(updated))
                showDatePickerTarget = null
            },
            onDismiss = { showDatePickerTarget = null }
        )
    }

    if (showColorPicker) {
        val colorHex = draft.colorHex ?: ""
        ColorPickerDialog(
            initialColor = try { parseColor(colorHex) } catch (e: Exception) { Color.Gray },
            onColorSelected = { 
                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(colorHex = it.toHex()))) 
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = stringResource(R.string.applications_kocolor_features_cosmetics_pick_color_title)
        )
    }

    if (!isEditMode && uiState.isScanSuccessful) {
        ValidateItemScreen(
            uiState = uiState,
            onEvent = onEvent,
            navTo = navTo
        )
        return
    }

    if (!isEditMode && uiState.isAnalyzing) {
        DiscoveryStatusScreen(
            status = uiState.discoveryStatus,
            mode = com.zoewave.probase.features.camera.productcapture.ui.DiscoveryMode.DETERMINISTIC,
            onBack = { onEvent(CosmeticsEvent.CancelDiscovery) },
            onNext = { onEvent(CosmeticsEvent.ContinueToReview) }
        )
        return
    }

    if (!isEditMode && uiState.scanState == FashionSessionRepository.ScanStatus.ANALYZING && uiState.aiResult == null) {
        // This is the Gemini phase
        DiscoveryStatusScreen(
            status = uiState.discoveryStatus,
            mode = com.zoewave.probase.features.camera.productcapture.ui.DiscoveryMode.AI_SYNTHESIS,
            onBack = { onEvent(CosmeticsEvent.CancelDiscovery) },
            onNext = { /* Final logic */ }
        )
        return
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onEvent(CosmeticsEvent.DeleteItem(draft.internalId))
                        showDeleteConfirmation = false
                        navTo(KoColorRoute.Back)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_delete_button), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_cancel))
                }
            },
            title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_delete_confirm_title)) },
            text = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_delete_confirm_message, draft.name)) },
            shape = RoundedCornerShape(24.dp)
        )
    }

    /*if (uiState.lastScanFailed && uiState.scanStatus != null) {
        AlertDialog(
            onDismissRequest = { onEvent(CosmeticsEvent.AcknowledgeErrorDialog) },
            confirmButton = {
                TextButton(onClick = { onEvent(CosmeticsEvent.AcknowledgeErrorDialog) }) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_ok), fontWeight = FontWeight.Bold)
                }
            },
            title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_not_found_title)) },
            text = { Text(uiState.scanStatus ?: "") },
            shape = RoundedCornerShape(24.dp)
        )
    }*/

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isEditMode) stringResource(R.string.applications_kocolor_features_cosmetics_edit_screen_title) 
                        else stringResource(R.string.applications_kocolor_features_cosmetics_add_to_collection), 
                        style = MaterialTheme.typography.titleLarge, 
                        fontFamily = FontFamily.Serif, 
                        color = atelierBrown,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        onEvent(CosmeticsEvent.CancelDiscovery)
                        navTo(KoColorRoute.Back) 
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_close_desc))
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_delete_desc), tint = Color.Gray)
                        }
                        IconButton(onClick = {
                            onEvent(CosmeticsEvent.UpdateItem(draft))
                            navTo(KoColorRoute.Back)
                        }) {
                            Icon(Icons.Default.Save, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_save_desc), tint = atelierBrown)
                        }
                    } else {
                        IconButton(onClick = { navTo(KoColorRoute.BoxCapture()) }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_scan_box_title), tint = atelierBrown)
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color.White)
        ) {
            // Hero Image
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clickable { navTo(KoColorRoute.BoxCapture(mode = "PRODUCT")) },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFBF8F5),
                border = BorderStroke(1.dp, Color(0xFFF0F0F0))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (draft.imageUrl != null) {
                        AsyncImage(
                            model = draft.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(40.dp), tint = atelierBrown.copy(alpha = 0.5f))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_add_product_image), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    // --- Scan Status Overlay ---
                    uiState.scanStatus?.let { status ->
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (uiState.isAnalyzing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(
                                        imageVector = if (uiState.lastScanFailed) Icons.Default.ErrorOutline else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (uiState.lastScanFailed) Color.Red else Color(0xFF22d3ee),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(status, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Capture Row
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CaptureButton(
                    title = "Bar Scan",
                    subtitle = when {
                        uiState.isAnalyzing -> "Analyzing..."
                        uiState.lastScanFailed -> "Not Found"
                        uiState.isScanIncomplete -> "Enrich Data"
                        else -> "Scan UPC"
                    },
                    icon = Icons.Default.QrCodeScanner,
                    color = when {
                        uiState.lastScanFailed -> Color(0xFFEF4444) // Red
                        uiState.isScanIncomplete -> Color(0xFFF59E0B) // Yellow/Amber
                        uiState.isAnalyzing -> Color(0xFF22d3ee) // Cyan
                        else -> Color(0xFF6B7280) // Gray
                    },
                    onClick = { 
                        onEvent(CosmeticsEvent.ResetScanState)
                        navTo(KoColorRoute.BarcodeScanner) 
                    },
                    modifier = Modifier.weight(1f),
                    isLoading = uiState.isAnalyzing
                )
                CaptureButton(
                    title = "Box Scan",
                    subtitle = "7-step AI",
                    icon = Icons.Default.AutoAwesome,
                    color = atelierBrown,
                    onClick = { navTo(KoColorRoute.BoxCapture(mode = "BOX")) },
                    modifier = Modifier.weight(1f)
                )
                CaptureButton(
                    title = "Product Scan",
                    subtitle = "5-step AI",
                    icon = Icons.Default.PhotoCamera,
                    color = Color(0xFFf472b6),
                    onClick = { navTo(KoColorRoute.BoxCapture(mode = "PRODUCT")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Core Identity
            AtelierExpandableSection(
                title = "Core Identity",
                isExpanded = expandedSections["Core"] == true,
                onToggle = { expandedSections["Core"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AtelierTextField(
                        value = draft.name,
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_product_name_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_product_name_placeholder)
                    )
                    AtelierTextField(
                        value = draft.brand,
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_brand_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_brand_placeholder)
                    )
                    AtelierTextField(
                        value = draft.batchCode ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(batchCode = it))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_sku_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_sku_placeholder)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AtelierTextField(
                            value = draft.shadeName ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_shade_name_label),
                            placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_shade_placeholder),
                            modifier = Modifier.weight(1f)
                        )
                        Column {
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_color), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                onClick = { showColorPicker = true },
                                shape = RoundedCornerShape(8.dp),
                                color = draft.colorHex?.let { parseColor(it) } ?: Color.White,
                                border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
                                modifier = Modifier.size(56.dp)
                            ) { }
                        }
                    }
                }
            }

            // Taxonomy & Facets
            AtelierExpandableSection(
                title = "Taxonomy & Facets",
                isExpanded = expandedSections["Taxonomy"] == true,
                onToggle = { expandedSections["Taxonomy"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfessionalDropdown(label = stringResource(R.string.applications_kocolor_features_cosmetics_macro_category_label), value = draft.macroCategory.displayName, onClick = { showMacroMenu = true })
                    ProfessionalDropdown(label = stringResource(R.string.applications_kocolor_features_cosmetics_micro_category_label), value = draft.microCategory.displayName, onClick = { showMicroMenu = true })
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfessionalDropdown(label = stringResource(R.string.applications_kocolor_features_cosmetics_formulation_label), value = draft.formulation.name.lowercase().capitalize(), onClick = { showFormulationMenu = true }, modifier = Modifier.weight(1f))
                        ProfessionalDropdown(label = stringResource(R.string.applications_kocolor_features_cosmetics_chemistry_label), value = draft.chemistryBase.name.lowercase().capitalize(), onClick = { showChemistryMenu = true }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfessionalDropdown(label = stringResource(R.string.applications_kocolor_features_cosmetics_finish_label), value = draft.finish.name.lowercase().capitalize(), onClick = { showFinishMenu = true }, modifier = Modifier.weight(1f))
                        ProfessionalDropdown(label = stringResource(R.string.applications_kocolor_features_cosmetics_coverage_label), value = draft.coverage.name.lowercase().replace("_", " ").capitalize(), onClick = { showCoverageMenu = true }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Clinical Safety
            AtelierExpandableSection(
                title = "Clinical Safety (FDA)",
                isExpanded = expandedSections["Safety"] == true,
                onToggle = { expandedSections["Safety"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.fdaDataVerified, onCheckedChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(fdaDataVerified = it))) })
                        Text("FDA Safety Checked", style = MaterialTheme.typography.bodyMedium)
                    }
                    AtelierTextField(
                        value = draft.fdaRecallStatus ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(fdaRecallStatus = it))) },
                        label = "Recall Status",
                        placeholder = "e.g. Active, None"
                    )
                    AtelierTextField(
                        value = draft.fdaAdverseEventCount.toString(),
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(fdaAdverseEventCount = it.toIntOrNull() ?: 0))) },
                        label = "Adverse Events Count"
                    )
                    AtelierTextField(
                        value = draft.fdaActiveIngredients.joinToString(", "),
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(fdaActiveIngredients = it.split(",").map { s -> s.trim() }))) },
                        label = "Active Ingredients",
                        placeholder = "Comma separated..."
                    )
                    AtelierTextField(
                        value = draft.fdaClinicalWarnings.joinToString("\n"),
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(fdaClinicalWarnings = it.split("\n")))) },
                        label = "Clinical Warnings",
                        minLines = 3
                    )
                }
            }

            // Ingredient Analysis
            AtelierExpandableSection(
                title = "Ingredient Analysis",
                isExpanded = expandedSections["Analysis"] == true,
                onToggle = { expandedSections["Analysis"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AtelierTextField(
                        value = draft.heroIngredient ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(heroIngredient = it))) },
                        label = "Hero Ingredient"
                    )
                    AtelierTextField(
                        value = draft.skinCompatibility ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(skinCompatibility = it))) },
                        label = "Skin Compatibility",
                        placeholder = "e.g. Universal, Oily, Sensitive"
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = draft.containsFragrance ?: false, onCheckedChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(containsFragrance = it))) })
                        Text("Contains Fragrance", style = MaterialTheme.typography.bodyMedium)
                    }
                    AtelierTextField(
                        value = draft.ingredients.joinToString(", "),
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(ingredients = it.split(",").map { s -> s.trim() }))) },
                        label = "Full Ingredients",
                        minLines = 4
                    )
                    AtelierTextField(
                        value = draft.allergens.joinToString(", "),
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(allergens = it.split(",").map { s -> s.trim() }))) },
                        label = "Allergen Alerts",
                        placeholder = "e.g. Nuts, Fragrance"
                    )
                }
            }

            // Sustainability
            AtelierExpandableSection(
                title = "Sustainability",
                isExpanded = expandedSections["Sustainability"] == true,
                onToggle = { expandedSections["Sustainability"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfessionalDropdown(
                        label = "Eco Score",
                        value = draft.ecoScore ?: "Not Rated",
                        onClick = {} // Could add a sub-menu for A-E
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = draft.isVegan ?: false, onCheckedChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(isVegan = it))) })
                            Text("Vegan", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = draft.isCrueltyFree ?: false, onCheckedChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(isCrueltyFree = it))) })
                            Text("Cruelty-Free", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    AtelierTextField(
                        value = draft.recyclingInstructions ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(recyclingInstructions = it))) },
                        label = "Recycling Guide"
                    )
                }
            }

            // Economics & Usage
            AtelierExpandableSection(
                title = "Economics & Usage",
                isExpanded = expandedSections["Economics"] == true,
                onToggle = { expandedSections["Economics"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AtelierTextField(label = "Price ($)", value = draft.price?.toString() ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) }, modifier = Modifier.weight(1f))
                        AtelierTextField(label = "Volume", value = draft.volume ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(volume = it))) }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AtelierTextField(label = "Amount Left", value = draft.amountRemaining?.toString() ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(amountRemaining = it.toDoubleOrNull()))) }, modifier = Modifier.weight(1f))
                        AtelierTextField(label = "Amount/Use", value = draft.amountPerUse?.toString() ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(amountPerUse = it.toDoubleOrNull()))) }, modifier = Modifier.weight(1f))
                    }
                    AtelierTextField(label = "Total Uses", value = draft.usageCount.toString(), onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(usageCount = it.toIntOrNull() ?: 0))) })
                    AtelierTextField(label = "Ritual Placement", value = draft.ritualPlacement ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(ritualPlacement = it))) }, placeholder = "e.g. Morning Step 2")
                }
            }

            // Lifecycle & Insights
            AtelierExpandableSection(
                title = "Lifecycle & Insights",
                isExpanded = expandedSections["Lifecycle"] == true,
                onToggle = { expandedSections["Lifecycle"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DatePickerButton(label = "Date Opened", timestamp = draft.openedDate, onClick = { showDatePickerTarget = "OPENED" })
                    DatePickerButton(label = "Expiry Date", timestamp = draft.expiryDate, onClick = { showDatePickerTarget = "EXPIRY" })
                    AtelierTextField(label = "PAO (Months)", value = draft.paoMonths?.toString() ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(paoMonths = it.toIntOrNull()))) })
                    
                    AtelierTextField(label = "Instructions", value = draft.instructions ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(instructions = it))) }, minLines = 3)
                    AtelierTextField(label = "Personal Notes", value = draft.notes ?: "", onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(notes = it))) }, minLines = 3)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Online Contribution Toggle
            if (!isEditMode) {
                ObfContributionSwitch(
                    uiState = uiState,
                    onEvent = onEvent
                )
                Spacer(Modifier.height(16.dp))
            }

            if (!isEditMode) {
                Button(
                    onClick = { 
                        onEvent(CosmeticsEvent.AddItem(draft))
                        navTo(KoColorRoute.Back)
                    },
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = atelierBrown)
                ) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_add_to_inventory_action), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(100.dp))
            
            // --- Menus ---
            DropdownMenu(expanded = showMacroMenu, onDismissRequest = { showMacroMenu = false }) {
                MacroCategory.entries.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.displayName) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(macroCategory = cat, microCategory = MicroCategory.entries.first { it.macro == cat })))
                        showMacroMenu = false
                    })
                }
            }
            DropdownMenu(expanded = showMicroMenu, onDismissRequest = { showMicroMenu = false }) {
                MicroCategory.entries.filter { it.macro == draft.macroCategory }.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.displayName) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(microCategory = cat, amountPerUse = cat.typicalAmountPerUse)))
                        showMicroMenu = false
                    })
                }
            }
            FormulationMenu(expanded = showFormulationMenu, onDismiss = { showFormulationMenu = false }, onSelect = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(formulation = it))) })
            ChemistryMenu(expanded = showChemistryMenu, onDismiss = { showChemistryMenu = false }, onSelect = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(chemistryBase = it))) })
            FinishMenu(expanded = showFinishMenu, onDismiss = { showFinishMenu = false }, onSelect = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(finish = it))) })
            CoverageMenu(expanded = showCoverageMenu, onDismiss = { showCoverageMenu = false }, onSelect = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(coverage = it))) })
        }
    }
}

@Composable
private fun CaptureButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(icon, null, tint = if (isColorDark(color)) Color.White else Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 12.sp)
            }
        }
    }
}

@Composable
private fun AtelierTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Color.LightGray) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFFE0E0E0),
                unfocusedBorderColor = Color(0xFFF0F0F0)
            ),
            minLines = minLines
        )
    }
}

@Composable
private fun ProfessionalDropdown(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(value, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
            }
        }
    }
}

@Composable
private fun DatePickerButton(label: String, timestamp: Long?, onClick: () -> Unit) {
    val dateText = timestamp?.let { 
        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
    } ?: "Not Set"
    
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
            color = Color.White
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(dateText, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AtelierDatePicker(initialDate: Long, onDateSelected: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onDateSelected(it) } }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = state)
    }
}

@Composable private fun FormulationMenu(expanded: Boolean, onDismiss: () -> Unit, onSelect: (Formulation) -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Formulation.entries.forEach { f -> DropdownMenuItem(text = { Text(f.name.lowercase().capitalize()) }, onClick = { onSelect(f); onDismiss() }) }
    }
}
@Composable private fun ChemistryMenu(expanded: Boolean, onDismiss: () -> Unit, onSelect: (ChemistryBase) -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        ChemistryBase.entries.forEach { c -> DropdownMenuItem(text = { Text(c.name.lowercase().capitalize()) }, onClick = { onSelect(c); onDismiss() }) }
    }
}
@Composable private fun FinishMenu(expanded: Boolean, onDismiss: () -> Unit, onSelect: (Finish) -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Finish.entries.forEach { f -> DropdownMenuItem(text = { Text(f.name.lowercase().capitalize()) }, onClick = { onSelect(f); onDismiss() }) }
    }
}
@Composable private fun CoverageMenu(expanded: Boolean, onDismiss: () -> Unit, onSelect: (Coverage) -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Coverage.entries.forEach { c -> DropdownMenuItem(text = { Text(c.name.lowercase().replace("_", " ").capitalize()) }, onClick = { onSelect(c); onDismiss() }) }
    }
}

@Composable
private fun ObfContributionSwitch(
    uiState: CosmeticsUiState,
    modifier: Modifier = Modifier,
    onEvent: (CosmeticsEvent) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .toggleable(
                value = uiState.isObfContributionEnabled,
                enabled = uiState.canContributeToObf,
                role = Role.Switch,
                onValueChange = { onEvent(CosmeticsEvent.OnObfContributionToggled(it)) }
            )
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Save & Add to Online DB",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (uiState.canContributeToObf) {
                    Color.Black
                } else {
                    Color.Gray
                }
            )
            Text(
                text = if (uiState.canContributeToObf) {
                    "Contribute this scan to the community."
                } else {
                    "Barcode required to contribute."
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        
        Switch(
            checked = uiState.isObfContributionEnabled,
            onCheckedChange = null,
            enabled = uiState.canContributeToObf
        )
    }
}

private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
