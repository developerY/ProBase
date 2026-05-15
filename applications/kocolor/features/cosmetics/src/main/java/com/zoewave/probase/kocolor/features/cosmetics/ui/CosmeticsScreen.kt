package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.CosmeticCategory
import com.zoewave.probase.kocolor.model.CosmeticItem

@Composable
fun CosmeticsUiRoute(
    uiState: Unit = Unit,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: CosmeticsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CosmeticsScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticsScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cosmetic Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOrderDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Application Order")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.items.isEmpty()) {
                Text(
                    "Your inventory is empty. Add your first beauty item!",
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                val allGroups = listOf(
                    "Face (Base & Coverage)",
                    "Cheeks (Color & Dimension)",
                    "Eyes (Definition)",
                    "Lips (Color & Texture)",
                    "Tools & Accessories"
                )

                val groupedBySection = remember(uiState.items) {
                    uiState.items.groupBy { it.category.groupName }
                }

                val expandedGroups = remember { 
                    mutableStateMapOf<String, Boolean>().apply {
                        allGroups.forEach { put(it, true) }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    allGroups.forEach { groupName ->
                        val itemsInGroup = groupedBySection[groupName] ?: emptyList()
                        
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedGroups[groupName] = !(expandedGroups[groupName] ?: true) }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = if (expandedGroups[groupName] == true) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (expandedGroups[groupName] == true) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    thickness = 2.dp
                                )
                            }
                        }

                        if (expandedGroups[groupName] == true) {
                            if (itemsInGroup.isEmpty()) {
                                item {
                                    Text(
                                        text = "No items in this section yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                val itemsByCategory = itemsInGroup.groupBy { it.category }
                                itemsByCategory.forEach { (category, items) ->
                                    item {
                                        Text(
                                            text = category.displayName,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                                        )
                                    }
                                    items(items) { item ->
                                        CosmeticCard(
                                            uiState = item,
                                            onEvent = { onEvent(CosmeticsEvent.DeleteItem(item.id)) },
                                            navTo = navTo
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCosmeticDialog(
            uiState = Unit,
            onEvent = { onEvent(CosmeticsEvent.AddItem(it)) },
            navTo = { showAddDialog = false }
        )
    }

    if (showOrderDialog) {
        MakeupOrderDialog(
            uiState = Unit,
            onEvent = {},
            navTo = { showOrderDialog = false }
        )
    }
}

@Composable
fun MakeupOrderDialog(
    uiState: Unit,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Standard Application Order", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    OrderSection(
                        uiState = "Phase 1: Skin Prep" to "Skincare (Cleanser, toner, moisturizer, sunscreen) followed by Primer.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 2: The Base" to "Foundation/Skin Tint, then Concealer for blemishes, and Setting Powder for oily areas.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 3: Dimension & Cheeks" to "Apply Cream products (blush/bronzer) now, then Powder products, and Highlighter last.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 4: Eyes & Brows" to "Eyebrows first to frame the face, then Eyeshadow, Eyeliner, and finally Mascara.",
                        onEvent = {},
                        navTo = {}
                    )
                }
                item {
                    OrderSection(
                        uiState = "Phase 5: Lips & Setting" to "Lip Liner to shape, Lipstick/Gloss to fill, and Setting Spray to lock it all in.",
                        onEvent = {},
                        navTo = {}
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Got it!") }
        }
    )
}

@Composable
fun OrderSection(
    uiState: Pair<String, String>,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Column {
        Text(
            text = uiState.first,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = uiState.second,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CosmeticCard(
    uiState: CosmeticItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colorHex = uiState.colorHex
            if (colorHex != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(uiState.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${uiState.brand} • ${uiState.category.name}", style = MaterialTheme.typography.bodySmall)
                if (!uiState.shadeName.isNullOrEmpty()) {
                    Text("Shade: ${uiState.shadeName}", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = { onEvent(Unit) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddCosmeticDialog(
    uiState: Unit,
    onEvent: (CosmeticItem) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CosmeticCategory.FOUNDATION) }
    var colorHex by remember { mutableStateOf("") }
    var shadeName by remember { mutableStateOf("") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { navTo(KoColorRoute.Back) },
        title = { Text("Add Cosmetic Item") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCategoryMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.displayName)
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 400.dp)
                        ) {
                            CosmeticCategory.entries.groupBy { it.groupName }.forEach { (group, items) ->
                                Text(
                                    text = group,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                items.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.displayName) },
                                        onClick = {
                                            category = cat
                                            showCategoryMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = colorHex,
                    onValueChange = { colorHex = it },
                    label = { Text("Color Hex (e.g., #FF0000)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = shadeName,
                    onValueChange = { shadeName = it },
                    label = { Text("Shade Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEvent(CosmeticItem(name = name, brand = brand, category = category, colorHex = colorHex.takeIf { it.isNotBlank() }, shadeName = shadeName.takeIf { it.isNotBlank() }))
                    navTo(KoColorRoute.Back)
                },
                enabled = name.isNotBlank() && brand.isNotBlank()
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = { navTo(KoColorRoute.Back) }) { Text("Cancel") }
        }
    )
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderSectionPreview() {
    MaterialTheme {
        OrderSection(
            uiState = "Phase 1" to "Description here",
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeupOrderDialogPreview() {
    MaterialTheme {
        MakeupOrderDialog(
            uiState = Unit,
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddCosmeticDialogPreview() {
    MaterialTheme {
        AddCosmeticDialog(
            uiState = Unit,
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CosmeticCardPreview() {
    MaterialTheme {
        CosmeticCard(
            uiState = CosmeticItem(
                name = "Velvet Matte",
                brand = "Sample Brand",
                category = CosmeticCategory.LIPSTICK,
                colorHex = "#FF0000",
                shadeName = "True Red"
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CosmeticsScreenPreview() {
    MaterialTheme {
        CosmeticsScreen(
            uiState = CosmeticsUiState(
                items = listOf(
                    CosmeticItem(name = "Luxe Glow", brand = "Brand A", category = CosmeticCategory.FOUNDATION),
                    CosmeticItem(name = "Night Berry", brand = "Brand B", category = CosmeticCategory.LIPSTICK, colorHex = "#800020")
                ),
                isLoading = false
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
