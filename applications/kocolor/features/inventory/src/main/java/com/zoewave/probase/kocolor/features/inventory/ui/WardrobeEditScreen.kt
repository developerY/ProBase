package com.zoewave.probase.kocolor.features.inventory.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WardrobeEditScreen(
    itemId: Long,
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LaunchedEffect(itemId) {
        onEvent(WardrobeEvent.InitializeEdit(itemId))
    }

    val draft = uiState.draftItem

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Garment", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        Text("Save")
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
            // Visual Preview Block
            Surface(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                color = draft.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }

            // Core Metadata
            OutlinedTextField(
                value = draft.name,
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(name = it))) },
                label = { Text("Garment Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = draft.brand ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(brand = it))) },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = draft.price?.toString() ?: "",
                    onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                    label = { Text("Price ($)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = draft.size ?: "",
                    onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(size = it))) },
                    label = { Text("Size") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Categories
            Column {
                Text("VERTICAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClothingCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = draft.category == cat,
                            onClick = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(category = cat))) },
                            label = { Text(cat.name) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Additional Details
            OutlinedTextField(
                value = draft.material ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(material = it))) },
                label = { Text("Material Composition") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = draft.notes ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(notes = it))) },
                label = { Text("Curator's Notes") },
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

@Preview(showBackground = true)
@Composable
private fun WardrobeEditScreenPreview() {
    WardrobeEditScreen(
        itemId = 1L,
        uiState = WardrobeUiState(
            draftItem = ClothingItem(
                id = 1L,
                name = "Blazer",
                category = ClothingCategory.TOPS
            )
        ),
        onEvent = {},
        navTo = {}
    )
}
