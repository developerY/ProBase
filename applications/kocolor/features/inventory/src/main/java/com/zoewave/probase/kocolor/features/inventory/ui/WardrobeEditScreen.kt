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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.ClothingCategory

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeEditScreen(
    itemId: Long,
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    LaunchedEffect(itemId) {
        if (itemId != 0L) {
            onEvent(WardrobeEvent.InitializeEdit(itemId))
        }
    }

    val draft = uiState.draftItem
    var showCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Edit Garment", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onEvent(WardrobeEvent.UpdateItem(draft))
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
                    .height(240.dp)
                    .clickable { navTo(KoColorRoute.Camera("clothing_item")) },
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (draft.imageUrl != null) {
                        AsyncImage(
                            model = draft.imageUrl,
                            contentDescription = "Garment Image",
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
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Update Garment Photo", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // 2. Form Fields
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    OutlinedButton(
                        onClick = { showCategoryMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(draft.category.name.lowercase().replaceFirstChar { it.uppercase() }, maxLines = 1)
                    }
                }
                
                OutlinedTextField(
                    value = draft.price?.toString() ?: "",
                    onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(price = it.toDoubleOrNull()))) },
                    label = { Text("Price") },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(16.dp),
                    prefix = { Text("$") }
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft.size ?: "",
                    onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(size = it))) },
                    label = { Text("Size") },
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
                        .clickable { /* Color Picker Later */ }
                )
            }

            OutlinedTextField(
                value = draft.material ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(material = it))) },
                label = { Text("Material") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            OutlinedTextField(
                value = draft.notes ?: "",
                onValueChange = { onEvent(WardrobeEvent.UpdateDraft(draft.copy(notes = it))) },
                label = { Text("Personal Notes") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                shape = RoundedCornerShape(16.dp),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Danger Zone
            TextButton(
                onClick = {
                    onEvent(WardrobeEvent.DeleteItem(itemId))
                    navTo(KoColorRoute.Back)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Remove from Wardrobe")
            }

            DropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false },
                modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 400.dp)
            ) {
                ClothingCategory.entries.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onEvent(WardrobeEvent.UpdateDraft(draft.copy(category = cat)))
                            showCategoryMenu = false
                        }
                    )
                }
            }
        }
    }
}
