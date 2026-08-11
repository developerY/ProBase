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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute

data class WardrobeEditUiState(
    val itemId: Long,
    val wardrobeUiState: WardrobeUiState
)

@Preview(showBackground = true)
@Composable
private fun WardrobeEditScreenPreview() {
    MaterialTheme {
        WardrobeEditScreen(
            uiState = WardrobeEditUiState(
                itemId = 1L,
                wardrobeUiState = WardrobeUiState(
                    draftItem = ClothingItem(
                        internalId = 1L,
                        name = "Blazer",
                        category = ClothingCategory.TOPS,
                        colorHex = "#000000"
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WardrobeEditScreen(
    uiState: WardrobeEditUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val itemId = uiState.itemId
    val state = uiState.wardrobeUiState
    
    LaunchedEffect(itemId) {
        onEvent(WardrobeEvent.InitializeEdit(itemId))
    }

    val draft = state.draftItem
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        val initialColorHex = draft.dominantHex ?: draft.colorHex ?: "#FFFFFF"
        ColorPickerDialog(
            initialColor = try { parseColor(initialColorHex) } catch (ignore: Exception) { Color.Gray },
            onColorSelected = { newColor ->
                onEvent(WardrobeEvent.UpdateDraft(draft.copy(dominantHex = newColor.toHex(), colorHex = newColor.toHex())))
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = stringResource(R.string.applications_kocolor_features_inventory_representative_color)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_inventory_edit), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onEvent(WardrobeEvent.UpdateItem(draft))
                            navTo(KoColorRoute.Back)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_save))
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
            // Visual Preview Block (Photo Picker)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { navTo(KoColorRoute.Camera("inventory_item")) },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (draft.imageUrl != null) {
                        AsyncImage(
                            model = draft.imageUrl,
                            contentDescription = stringResource(R.string.applications_kocolor_features_inventory_garment_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt, 
                                null, 
                                modifier = Modifier.size(48.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.applications_kocolor_features_inventory_take_photo), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Representative Color Refinement
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_representative_color), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Text(stringResource(R.string.applications_kocolor_features_inventory_tweak_hue), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                
                val itemColorHex = draft.dominantHex ?: draft.colorHex ?: "#CCCCCC"
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showColorPicker = true }
                        .background(parseColor(itemColorHex))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    color = parseColor(itemColorHex)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Colorize, null, modifier = Modifier.size(20.dp), tint = if (isColorDark(parseColor(itemColorHex))) Color.White else Color.Black)
                    }
                }
            }

            // Core Metadata
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(name = it))) },
                label = { Text(stringResource(R.string.applications_kocolor_features_inventory_garment_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = draft.brand ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(brand = it))) },
                label = { Text(stringResource(R.string.applications_kocolor_features_inventory_brand)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = draft.price?.toString() ?: "",
                    onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                    label = { Text(stringResource(R.string.applications_kocolor_features_inventory_price)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = draft.size ?: "",
                    onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(size = it))) },
                    label = { Text(stringResource(R.string.applications_kocolor_features_inventory_size)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Categories
            Column {
                Text("VERTICAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClothingCategory.entries.forEach { categoryEnum ->
                        FilterChip(
                            selected = draft.category == categoryEnum,
                            onClick = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(category = categoryEnum))) },
                            label = { Text(categoryEnum.name) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Additional Details
            OutlinedTextField(
                value = draft.material ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(material = it))) },
                label = { Text(stringResource(R.string.applications_kocolor_features_inventory_material_composition)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = draft.notes ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(notes = it))) },
                label = { Text(stringResource(R.string.applications_kocolor_features_inventory_curators_notes)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    onEvent(WardrobeEvent.DeleteItem(itemId))
                    navTo(KoColorRoute.Back)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Remove from Wardrobe")
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}
