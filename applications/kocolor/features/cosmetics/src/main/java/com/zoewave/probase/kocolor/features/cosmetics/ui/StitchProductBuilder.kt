package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.CategoryIconItem
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.CategoryIconUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun StitchProductBuilderPreview() {
    MaterialTheme {
        StitchProductBuilder(
            uiState = CosmeticsUiState(),
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
            title = stringResource(R.string.applications_kocolor_features_cosmetics_pick_color_title)
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
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_add_to_collection), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, color = Color(0xFF8B5E3C)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        onEvent(CosmeticsEvent.CancelDiscovery)
                        navTo(KoColorRoute.Back) 
                    }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_close_desc))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.BoxCapture()) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_scan_box_title), tint = Color(0xFF8B5E3C))
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_add_product_image), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.Black)
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_tap_to_upload), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            if (uiState.lastScanFailed) {
                AlertDialog(
                    onDismissRequest = { onEvent(CosmeticsEvent.ResetScanState) },
                    confirmButton = {
                        TextButton(onClick = { onEvent(CosmeticsEvent.ResetScanState) }) {
                            Text(stringResource(R.string.applications_kocolor_features_cosmetics_ok), fontWeight = FontWeight.Bold)
                        }
                    },
                    title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_not_found_title)) },
                    text = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_not_found_message)) },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Barcode Scan
                Surface(
                    onClick = { navTo(KoColorRoute.BarcodeScanner) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF6B7280).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFF6B7280).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_cosmetics_barcode_scan_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                            Text(
                                text = "Scan UPC",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }

                // Pro Scan
                Surface(
                    onClick = { navTo(KoColorRoute.BoxCapture(mode = "BOX")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF8B5E3C).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFF8B5E3C).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF8B5E3C),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_cosmetics_scan_box_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5E3C)
                            )
                            Text(
                                text = "7-angle AI",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }

                // Quick Scan
                Surface(
                    onClick = { navTo(KoColorRoute.BoxCapture(mode = "QUICK_BOX")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF22d3ee).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFF22d3ee).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF22d3ee),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_cosmetics_quick_scan_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0891b2)
                            )
                            Text(
                                text = "3-pic AI",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.applications_kocolor_features_cosmetics_manual_entry), style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_category_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val categories = listOf(
                        Triple(MacroCategory.COMPLEXION, Icons.Default.Face, stringResource(R.string.applications_kocolor_features_cosmetics_category_section)),
                        Triple(MacroCategory.PREP, Icons.Default.Opacity, "Skincare"), 
                        Triple(MacroCategory.EYES, Icons.Default.Visibility, "Eyes"),
                        Triple(MacroCategory.LIPS, Icons.Default.Favorite, "Lips")
                    )
                    categories.forEach { (cat, icon, label) ->
                        val isSelected = draft.macroCategory == cat
                        CategoryIconItem(
                            uiState = CategoryIconUiState(
                                icon = icon,
                                label = label,
                                isSelected = isSelected
                            ),
                            modifier = Modifier.weight(1f),
                            onEvent = { 
                                onEvent(CosmeticsEvent.UpdateDraft(draft.copy(
                                    macroCategory = cat,
                                    microCategory = MicroCategory.entries.first { it.macro == cat }
                                )))
                            },
                            navTo = {}
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_subcategory_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_brand_name_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                OutlinedTextField(
                    value = draft.brand,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(brand = it))) },
                    placeholder = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_brand_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_name_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(name = it))) },
                    placeholder = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_product_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_shade_name_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = draft.shadeName ?: "",
                        onValueChange = { onEvent(CosmeticsEvent.UpdateDraft(draft.copy(shadeName = it))) },
                        placeholder = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_shade_placeholder)) },
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
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_add_to_inventory_action), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
