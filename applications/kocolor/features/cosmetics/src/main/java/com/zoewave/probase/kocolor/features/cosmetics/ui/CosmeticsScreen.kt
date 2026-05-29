package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.isColorDark
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex
import com.zoewave.probase.kocolor.model.*

@Preview(showBackground = true)
@Composable
private fun CosmeticsUiRoutePreview() {
    MaterialTheme {
        CosmeticsUiRoute(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@Composable
fun CosmeticsUiRoute(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    CosmeticsScreen(
        uiState = uiState,
        onEvent = onEvent,
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
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.CosmeticAdd()) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        onEvent(CosmeticsEvent.UpdateSortOption(option))
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.sortOption == option) {
                                            Icon(Icons.Default.Check, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navTo(KoColorRoute.CosmeticAdd()) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(CosmeticsEvent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name, brand, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val groupedByMacro = remember(uiState.filteredItems) {
                        uiState.filteredItems.groupBy { it.macroCategory }
                    }

                    val expandedMacros = remember { 
                        mutableStateMapOf<MacroCategory, Boolean>().apply {
                            MacroCategory.entries.forEach { put(it, true) }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MacroCategory.entries.forEach { macro ->
                            val itemsInMacro = groupedByMacro[macro] ?: emptyList()
                            if (itemsInMacro.isEmpty() && uiState.searchQuery.isNotEmpty()) return@forEach

                            val isExpanded = expandedMacros[macro] == true
                            
                            item {
                                GroupSectionCard(
                                    title = macro.displayName,
                                    itemCount = itemsInMacro.size,
                                    isExpanded = isExpanded,
                                    onToggle = { expandedMacros[macro] = it }
                                )
                            }

                            if (isExpanded) {
                                val groupedByMicro = itemsInMacro.groupBy { it.microCategory }
                                
                                groupedByMicro.forEach { (micro, itemsInMicro) ->
                                    item {
                                        SubCategoryCard(
                                            micro = micro,
                                            itemCount = itemsInMicro.size,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }

                                    items(itemsInMicro) { item ->
                                        CosmeticProductCard(
                                            uiState = item,
                                            onEvent = onEvent,
                                            navTo = navTo,
                                            modifier = Modifier.padding(start = 32.dp)
                                        )
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupSectionCard(
    title: String,
    itemCount: Int,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        onClick = { onToggle(!isExpanded) }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(text = "$itemCount items", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
    }
}

@Composable
fun SubCategoryCard(
    micro: MicroCategory,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = micro.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                Text(text = itemCount.toString(), modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
fun CosmeticProductCard(
    uiState: CosmeticItem,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = uiState.colorHex?.let { parseColor(it) } ?: MaterialTheme.colorScheme.surface
    val contentColor = if (uiState.colorHex != null && isColorDark(cardColor)) Color.White else Color.Black

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { navTo(KoColorRoute.CosmeticDetail(uiState.id)) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor, contentColor = contentColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Product Image or Swatch
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(contentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUrl != null) {
                        AsyncImage(model = uiState.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.AutoAwesome, null, tint = contentColor.copy(alpha = 0.5f))
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = uiState.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = uiState.brand, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
                }
                
                IconButton(onClick = { onEvent(CosmeticsEvent.DeleteItem(uiState.id)) }) {
                    Icon(Icons.Default.Delete, null, tint = contentColor.copy(alpha = 0.6f))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("COST / USE", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                    Text(text = uiState.costPerUse?.let { "$%.2f".format(it) } ?: "N/A", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onEvent(CosmeticsEvent.UseItem(uiState.id)) },
                    colors = ButtonDefaults.buttonColors(containerColor = contentColor.copy(alpha = 0.2f), contentColor = contentColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("USE", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
