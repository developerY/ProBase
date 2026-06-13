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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.model.*
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.toHex

data class CosmeticEditUiState(
    val itemId: Long,
    val draftItem: CosmeticItem
)

@Preview(showBackground = true)
@Composable
private fun CosmeticEditScreenPreview() {
    MaterialTheme {
        CosmeticEditScreen(
            uiState = CosmeticEditUiState(
                itemId = 1L,
                draftItem = CosmeticItem(
                    id = 1L,
                    name = "Cool Ivory",
                    brand = "KoColor",
                    macroCategory = MacroCategory.COMPLEXION,
                    microCategory = MicroCategory.FOUNDATION,
                    price = 42.0,
                    volume = "30ml",
                    amountPerUse = 0.35,
                    shadeName = "Cool",
                    colorHex = "#FAD4D4"
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticEditScreen(
    uiState: CosmeticEditUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val itemId = uiState.itemId
    val draft = uiState.draftItem
    val atelierBrown = Color(0xFF8B5E3C)
    
    LaunchedEffect(itemId) {
        if (itemId != 0L) {
            onEvent(CosmeticsEvent.InitializeEdit(itemId))
        }
    }

    var showMacroMenu by remember { mutableStateOf(false) }
    var showMicroMenu by remember { mutableStateOf(false) }
    var showFormulationMenu by remember { mutableStateOf(false) }
    var showChemistryMenu by remember { mutableStateOf(false) }
    var showFinishMenu by remember { mutableStateOf(false) }
    var showCoverageMenu by remember { mutableStateOf(false) }
    
    var showColorPicker by remember { mutableStateOf(false) }
    var showDatePickerTarget by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onEvent(CosmeticsEvent.DeleteItem(itemId))
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

    val expandedSections = remember { 
        mutableStateMapOf<String, Boolean>().apply {
            put("Category", true)
            put("Facets", true)
            put("Physical", true)
            put("Economics", true)
            put("Lifecycle", true)
            put("Insights", true)
        }
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.applications_kocolor_features_cosmetics_edit_screen_title), 
                        style = MaterialTheme.typography.titleLarge, 
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = atelierBrown
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_close_desc))
                    }
                },
                actions = {
                    if (itemId == 0L) {
                        IconButton(onClick = { navTo(KoColorRoute.BoxCapture) }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Scan Box", tint = atelierBrown)
                        }
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_delete_desc), tint = Color.Gray)
                    }
                    IconButton(onClick = {
                        onEvent(CosmeticsEvent.UpdateItem(draft))
                        navTo(KoColorRoute.Back)
                    }) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_save_desc), tint = atelierBrown)
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
                .background(Color.White)
        ) {
            // 1. Hero Image Selection
            Surface(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
                color = Color(0xFFFBF8F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (draft.imageUrl != null) {
                        AsyncImage(
                            model = draft.imageUrl,
                            contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_product_image_desc),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(40.dp), tint = atelierBrown.copy(alpha = 0.5f))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_photo), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            // 2. Core Identity
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
            }

            // 3. Category & Facets
            EditExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_category_taxonomy_title),
                isExpanded = expandedSections["Category"] == true,
                onToggle = { expandedSections["Category"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfessionalDropdown(
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_macro_category_label),
                        value = draft.macroCategory.displayName,
                        onClick = { showMacroMenu = true }
                    )
                    ProfessionalDropdown(
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_micro_category_label),
                        value = draft.microCategory.displayName,
                        onClick = { showMicroMenu = true }
                    )
                }
            }

            EditExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_professional_facets_title),
                isExpanded = expandedSections["Facets"] == true,
                onToggle = { expandedSections["Facets"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

            // 4. Physical & Color
            EditExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_physical_color_title),
                isExpanded = expandedSections["Physical"] == true,
                onToggle = { expandedSections["Physical"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AtelierTextField(
                        value = draft.shadeName ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_shade_name_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_shade_placeholder)
                    )
                    
                    Column {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_color), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            onClick = { showColorPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            color = draft.colorHex?.let { parseColor(it) } ?: Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            modifier = Modifier.size(56.dp)
                        ) { }
                    }
                }
            }

            // 5. Inventory & Economics
            EditExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_inventory_economics_title),
                isExpanded = expandedSections["Economics"] == true,
                onToggle = { expandedSections["Economics"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AtelierTextField(
                            value = draft.price?.toString() ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_unit_price_label),
                            modifier = Modifier.weight(1f)
                        )
                        AtelierTextField(
                            value = draft.volume ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(volume = it))) },
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_total_volume_label),
                            placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_volume_placeholder),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AtelierTextField(
                            value = draft.amountRemaining?.toString() ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(amountRemaining = it.toDoubleOrNull()))) },
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_remaining_label),
                            modifier = Modifier.weight(1f)
                        )
                        AtelierTextField(
                            value = draft.amountPerUse?.toString() ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(amountPerUse = it.toDoubleOrNull()))) },
                            label = stringResource(R.string.applications_kocolor_features_cosmetics_amount_per_use_label),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 6. Lifecycle Dates
            EditExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_product_lifecycle_title),
                isExpanded = expandedSections["Lifecycle"] == true,
                onToggle = { expandedSections["Lifecycle"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DatePickerButton(
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_date_opened_label),
                        timestamp = draft.openedDate,
                        onClick = { showDatePickerTarget = "OPENED" }
                    )
                    DatePickerButton(
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_expiry_date_label),
                        timestamp = draft.expiryDate,
                        onClick = { showDatePickerTarget = "EXPIRY" }
                    )
                    
                    AtelierTextField(
                        value = draft.paoMonths?.toString() ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(paoMonths = it.toIntOrNull()))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_pao_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_pao_placeholder)
                    )
                }
            }

            // 7. Insights
            EditExpandableSection(
                title = stringResource(R.string.applications_kocolor_features_cosmetics_artist_insights_title),
                isExpanded = expandedSections["Insights"] == true,
                onToggle = { expandedSections["Insights"] = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AtelierTextField(
                        value = draft.instructions ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(instructions = it))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_instructions_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_instructions_placeholder),
                        minLines = 3
                    )
                    AtelierTextField(
                        value = draft.notes ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(notes = it))) },
                        label = stringResource(R.string.applications_kocolor_features_cosmetics_personal_notes_label),
                        placeholder = stringResource(R.string.applications_kocolor_features_cosmetics_notes_placeholder),
                        minLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))

            // --- Menus ---
            
            DropdownMenu(expanded = showMacroMenu, onDismissRequest = { showMacroMenu = false }) {
                MacroCategory.entries.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.displayName) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(
                            macroCategory = cat,
                            microCategory = MicroCategory.entries.first { it.macro == cat }
                        )))
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
private fun EditExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFF0F0F0))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isExpanded) }
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title.uppercase(), style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp), fontWeight = FontWeight.Black)
            Icon(imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                content()
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
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFE0E0E0),
                unfocusedIndicatorColor = Color(0xFFF0F0F0)
            ),
            minLines = minLines
        )
    }
}

@Composable
private fun DatePickerButton(
    label: String,
    timestamp: Long?,
    onClick: () -> Unit
) {
    val dateText = timestamp?.let { 
        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
    } ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_set)
    
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
private fun AtelierDatePicker(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onDateSelected(it) } }) { Text(stringResource(R.string.applications_kocolor_features_cosmetics_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.applications_kocolor_features_cosmetics_cancel)) }
        }
    ) {
        DatePicker(state = state)
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

private fun String.capitalize() = this.replaceFirstChar { it.uppercase() }
