package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StitchProductBuilder(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.draftItem
    val scrollState = rememberScrollState()
    var showColorPicker by remember { mutableStateOf(false) }

    BackHandler {
        onEvent(CosmeticsEvent.CancelDiscovery)
        navTo(KoColorRoute.Back)
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
            title = "Pick Product Color"
        )
    }

    if (uiState.isScanSuccessful) {
        ValidateItemScreen(
            uiState = uiState,
            onEvent = onEvent,
            navTo = navTo
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add to Collection", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, color = Color(0xFF8B5E3C)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        onEvent(CosmeticsEvent.CancelDiscovery)
                        navTo(KoColorRoute.Back) 
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Image Capture Area (Dotted border style)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF9F6F0),
                border = BorderStroke(1.dp, color = Color.LightGray.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (draft.imageUrl != null) {
                        AsyncImage(
                            model = draft.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF8B5E3C))
                            Spacer(Modifier.height(12.dp))
                            Text("ADD PRODUCT IMAGE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.Black)
                            Text("Tap to upload or take a photo", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            // 2. Barcode Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BARCODE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = draft.batchCode ?: "",
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(batchCode = it))) },
                    placeholder = { Text("Enter barcode manually...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.lastScanFailed) {
                                Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.padding(end = 8.dp))
                            }
                            IconButton(onClick = { navTo(KoColorRoute.BarcodeScanner) }) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = Color(0xFF8B5E3C))
                            }
                        }
                    },
                    isError = uiState.lastScanFailed,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }
            
            if (uiState.lastScanFailed) {
                AlertDialog(
                    onDismissRequest = { onEvent(CosmeticsEvent.ResetScanState) },
                    confirmButton = {
                        TextButton(onClick = { onEvent(CosmeticsEvent.ResetScanState) }) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = { Text("Product Not Found") },
                    text = { Text("The barcode could not be found in our database. Please enter the details manually.") },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // 3. OR Divider
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.3f))
                Text(" OR ", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.3f))
            }

            Text("Manual Entry", style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)

            // 4. Category Icons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val categories = listOf(
                        Triple(MacroCategory.COMPLEXION, Icons.Default.Face, "Complexion"),
                        Triple(MacroCategory.PREP, Icons.Default.Opacity, "Skincare"),
                        Triple(MacroCategory.EYES, Icons.Default.Visibility, "Eyes"),
                        Triple(MacroCategory.LIPS, Icons.Default.Favorite, "Lips")
                    )
                    categories.forEach { (cat, icon, label) ->
                        val isSelected = draft.macroCategory == cat
                        CategoryIconItem(
                            icon = icon,
                            label = label,
                            isSelected = isSelected,
                            onClick = { 
                                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(
                                    macroCategory = cat,
                                    microCategory = MicroCategory.entries.first { it.macro == cat }
                                )))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Sub-Category Dropdown
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SUB-CATEGORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                var showSubMenu by remember { mutableStateOf(false) }
                Box {
                    OutlinedTextField(
                        value = draft.microCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { showSubMenu = true },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = Color.White
                        )
                    )
                    DropdownMenu(expanded = showSubMenu, onDismissRequest = { showSubMenu = false }) {
                        MicroCategory.entries.filter { it.macro == draft.macroCategory }.forEach { micro ->
                            DropdownMenuItem(text = { Text(micro.displayName) }, onClick = {
                                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(microCategory = micro)))
                                showSubMenu = false
                            })
                        }
                    }
                }
            }

            // 6. Brand Name
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BRAND NAME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                OutlinedTextField(
                    value = draft.brand,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                    placeholder = { Text("e.g. Chanel, NARS...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            // 7. Product Name
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRODUCT NAME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                    placeholder = { Text("e.g. Les Beiges Water-Fresh Tint") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            // 8. Shade Name / Number
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SHADE NAME / NUMBER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.shadeName ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                        placeholder = { Text("e.g. Light, Medium Plus, 02...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    Surface(
                        onClick = { showColorPicker = true },
                        shape = CircleShape,
                        color = draft.colorHex?.let { parseColor(it) } ?: Color.White,
                        border = BorderStroke(1.dp, Color.LightGray),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val tintColor = draft.colorHex?.let { hex ->
                                if (isColorDark(parseColor(hex))) Color.White else Color.Black
                            } ?: Color.Black
                            Icon(Icons.Default.Colorize, null, modifier = Modifier.size(20.dp), tint = tintColor)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { 
                    onEvent(CosmeticsEvent.AddItem(draft))
                    navTo(KoColorRoute.Back)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5E3C))
            ) {
                Text("ADD TO INVENTORY", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CategoryIconItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color.White else Color(0xFFF5F5F5),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF8B5E3C).copy(alpha = 0.5f)) else null,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = if (isSelected) Color(0xFF8B5E3C) else Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF8B5E3C) else Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidateItemScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val draft = uiState.draftItem
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Validate Item", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, color = Color(0xFF8B5E3C)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CosmeticsEvent.ResetScanState) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Image with Captured Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFF9F6F0))
            ) {
                if (draft.imageUrl != null) {
                    AsyncImage(
                        model = draft.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(16.dp), tint = Color(0xFF8B5E3C))
                        Spacer(Modifier.width(8.dp))
                        Text("Captured", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text("Scanned Details", style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Brand Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = draft.brand,
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Product Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = draft.name,
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TaxonomyDropdown(
                                current = draft.macroCategory.displayName,
                                items = MacroCategory.entries.map { it.displayName },
                                onSelect = { name ->
                                    val cat = MacroCategory.entries.first { it.displayName == name }
                                    onEvent(CosmeticsEvent.UpdateDraft(draft.copy(macroCategory = cat, microCategory = MicroCategory.entries.first { it.macro == cat })))
                                }
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Sub-Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                            TaxonomyDropdown(
                                current = draft.microCategory.displayName,
                                items = MicroCategory.entries.filter { it.macro == draft.macroCategory }.map { it.displayName },
                                onSelect = { name ->
                                    val micro = MicroCategory.entries.first { it.displayName == name }
                                    onEvent(CosmeticsEvent.UpdateDraft(draft.copy(microCategory = micro)))
                                }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Shade Name / Number", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(
                            value = draft.shadeName ?: "",
                            onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Actions
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { 
                        onEvent(CosmeticsEvent.AddItem(draft))
                        onEvent(CosmeticsEvent.ResetScanState)
                        navTo(KoColorRoute.Back)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5E3C))
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CONFIRM & ADD", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                OutlinedButton(
                    onClick = { 
                        onEvent(CosmeticsEvent.ResetScanState)
                        navTo(KoColorRoute.BarcodeScanner) 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("RE-SCAN", color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TaxonomyDropdown(
    current: String,
    items: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.White
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { label ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    onSelect(label)
                    expanded = false
                })
            }
        }
    }
}
