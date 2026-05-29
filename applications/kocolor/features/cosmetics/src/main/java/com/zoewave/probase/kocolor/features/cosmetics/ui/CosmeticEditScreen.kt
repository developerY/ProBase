package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
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

    if (showColorPicker) {
        val colorHex = draft.colorHex ?: ""
        ColorPickerDialog(
            initialColor = try { parseColor(colorHex) } catch (e: Exception) { Color.Gray },
            onColorSelected = { 
                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(colorHex = it.toHex()))) 
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Pick Product Color"
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Professional Edit", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onEvent(CosmeticsEvent.UpdateItem(draft))
                        navTo(KoColorRoute.Back)
                    }) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Photo Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (draft.imageUrl != null) {
                        AsyncImage(
                            model = draft.imageUrl,
                            contentDescription = "Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Product Photo", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // 2. Identity
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = draft.brand,
                onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // 3. Taxonomy (Progressive Disclosure)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("TAXONOMY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                
                // Macro Selection
                ProfessionalDropdown(
                    label = "Macro Category",
                    value = draft.macroCategory.displayName,
                    onClick = { showMacroMenu = true }
                )
                
                // Micro Selection (Disclosure)
                ProfessionalDropdown(
                    label = "Micro Category",
                    value = draft.microCategory.displayName,
                    onClick = { showMicroMenu = true }
                )
            }

            // 4. Professional Facets
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("PROFESSIONAL FACETS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfessionalDropdown(
                        label = "Formulation",
                        value = draft.formulation.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { showFormulationMenu = true },
                        modifier = Modifier.weight(1f)
                    )
                    ProfessionalDropdown(
                        label = "Chemistry",
                        value = draft.chemistryBase.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { showChemistryMenu = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfessionalDropdown(
                        label = "Finish",
                        value = draft.finish.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { showFinishMenu = true },
                        modifier = Modifier.weight(1f)
                    )
                    ProfessionalDropdown(
                        label = "Coverage",
                        value = draft.coverage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                        onClick = { showCoverageMenu = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Physical Properties
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft.shadeName ?: "",
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                    label = { Text("Shade Name") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                
                val colorHex = draft.colorHex ?: "#CCCCCC"
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(parseColor(colorHex))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { showColorPicker = true }
                )
            }

            // 6. Economics & Usage
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = draft.price?.toString() ?: "",
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                    label = { Text("Price ($)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = draft.volume ?: "",
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(volume = it))) },
                    label = { Text("Volume") },
                    placeholder = { Text("e.g. 30ml") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            
            OutlinedTextField(
                value = draft.amountPerUse?.toString() ?: "",
                onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(amountPerUse = it.toDoubleOrNull()))) },
                label = { Text("Amount Per Use") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                supportingText = { Text("Rec: %.2f".format(draft.microCategory.typicalAmountPerUse)) }
            )

            // 7. Notes
            OutlinedTextField(
                value = draft.notes ?: "",
                onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(notes = it))) },
                label = { Text("Artist Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- Dropdown Menus ---
            
            DropdownMenu(expanded = showMacroMenu, onDismissRequest = { showMacroMenu = false }) {
                MacroCategory.entries.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.displayName) },
                        onClick = {
                            onEvent(CosmeticsEvent.UpdateDraft(draft.copy(
                                macroCategory = cat,
                                microCategory = MicroCategory.entries.first { it.macro == cat }
                            )))
                            showMacroMenu = false
                        }
                    )
                }
            }
            
            DropdownMenu(expanded = showMicroMenu, onDismissRequest = { showMicroMenu = false }) {
                MicroCategory.entries.filter { it.macro == draft.macroCategory }.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.displayName) },
                        onClick = {
                            onEvent(CosmeticsEvent.UpdateDraft(draft.copy(
                                microCategory = cat,
                                amountPerUse = cat.typicalAmountPerUse
                            )))
                            showMicroMenu = false
                        }
                    )
                }
            }
            
            DropdownMenu(expanded = showFormulationMenu, onDismissRequest = { showFormulationMenu = false }) {
                Formulation.entries.forEach { f ->
                    DropdownMenuItem(text = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(formulation = f)))
                        showFormulationMenu = false
                    })
                }
            }
            
            DropdownMenu(expanded = showChemistryMenu, onDismissRequest = { showChemistryMenu = false }) {
                ChemistryBase.entries.forEach { c ->
                    DropdownMenuItem(text = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(chemistryBase = c)))
                        showChemistryMenu = false
                    })
                }
            }
            
            DropdownMenu(expanded = showFinishMenu, onDismissRequest = { showFinishMenu = false }) {
                Finish.entries.forEach { f ->
                    DropdownMenuItem(text = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(finish = f)))
                        showFinishMenu = false
                    })
                }
            }
            
            DropdownMenu(expanded = showCoverageMenu, onDismissRequest = { showCoverageMenu = false }) {
                Coverage.entries.forEach { c ->
                    DropdownMenuItem(text = { Text(c.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }) }, onClick = {
                        onEvent(CosmeticsEvent.UpdateDraft(draft.copy(coverage = c)))
                        showCoverageMenu = false
                    })
                }
            }
        }
    }
}

@Composable
private fun ProfessionalDropdown(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
    }
}
