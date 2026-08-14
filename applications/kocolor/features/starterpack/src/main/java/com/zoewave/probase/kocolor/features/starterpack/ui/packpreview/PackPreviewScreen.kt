package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.CosmeticItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import com.zoewave.probase.kocolor.features.starterpack.ui.PackPreviewUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PackPreviewScreen(
    uiState: PackPreviewUiState,
    onToggleSelection: (String) -> Unit,
    onToggleCollapse: (String) -> Unit,
    onSelectCategoryAll: (String) -> Unit,
    onClearCategory: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onWipeCollection: () -> Unit,
    onImportSelected: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleValueSort: () -> Unit,
    onItemInfoClick: (String) -> Unit,
    onDismissNotes: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    var showWipeConfirm by remember { mutableStateOf(false) }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text("Wipe this Collection?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove all items from this collection. Your personal 'Make it Mine' items are safe.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onWipeCollection()
                        showWipeConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("WIPE DATA", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text("CANCEL")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    LaunchedEffect(uiState.targetItemId) {
        val targetId = uiState.targetItemId
        if (targetId != null) {
            val index = uiState.items.indexOfFirst { it.id == targetId }
            if (index != -1) {
                // Approximate index finding when grouped is harder, 
                // but for now we scroll to first item if it's visible or handle basic list.
                // In grouped LazyColumn, we'd need to calculate the actual position including headers.
            }
        }
    }

    ProductEditorialNotesDialog(
        notes = uiState.selectedItemNotes,
        thumbnailUrl = uiState.selectedItemThumbnail,
        colorHex = uiState.selectedItemColor,
        isLoading = uiState.isNotesLoading,
        onDismiss = onDismissNotes
    )

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.surface, // Match opaque headers
        topBar = {
            PackPreviewTopAppBar(
                onBack = onBack,
                onSelectAll = onSelectAll,
                onClear = onDeselectAll
            )
        },
        bottomBar = {
            PackPreviewBottomBar(
                selectedCount = uiState.selectedIds.size,
                isLoading = uiState.isLoading,
                onImportSelected = onImportSelected,
                onWipe = { showWipeConfirm = true },
                isWipeVisible = uiState.isInstalled
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF745E7A))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Digital Counter Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Search products or actives...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color.White
                        )
                    )
                    
                    FilterChip(
                        selected = uiState.sortByValue,
                        onClick = onToggleValueSort,
                        label = { 
                            Text(
                                text = if (uiState.sortByValue) "Best Value First" else "Value Sorting", 
                                style = MaterialTheme.typography.labelSmall 
                            ) 
                        },
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = if (uiState.sortByValue) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    uiState.groupedItems.forEach { (category, items) ->
                        val isCollapsed = uiState.collapsedCategories.contains(category)
                        val categorySelectedCount = items.count { uiState.selectedIds.contains(it.id) }

                        stickyHeader(key = "header_$category") {
                            PackPreviewCategoryHeader(
                                categoryName = category,
                                selectedCount = categorySelectedCount,
                                totalCount = items.size,
                                isCollapsed = isCollapsed,
                                onToggleCollapse = { onToggleCollapse(category) },
                                onSelectAll = { onSelectCategoryAll(category) },
                                onClear = { onClearCategory(category) }
                            )
                        }

                        if (!isCollapsed) {
                            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                                PackPreviewItemRow(
                                    item = item,
                                    isSelected = uiState.selectedIds.contains(item.id),
                                    isTarget = item.id == uiState.targetItemId,
                                    onInfoClick = { onItemInfoClick(item.id) },
                                    onSelectClick = { onToggleSelection(item.id) }
                                )
                                if (index < items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PackPreviewScreenPreview() {
    val mockItem1 = CosmeticItemDto(
        id = "1",
        name = "KoColor Purifying Gel Cleanser",
        brand = "KoColor",
        shadeName = "Clear Crystal",
        colorHex = "#F4F6F0",
        thumbnailUrl = "",
        imageUrl = "",
        macroCategory = "PREP",
        microCategory = "CLEANSER",
        price = 18.0,
        notes = null,
        formulation = "GEL",
        chemistryBase = "WATER",
        finish = "NATURAL",
        coverage = "SHEER",
        temperature = "NEUTRAL",
        volume = "150ml",
        paoMonths = 12,
        expiryDate = null,
        instructions = null,
        ingredients = emptyList(),
        allergens = emptyList(),
        isVegan = true,
        isCrueltyFree = true,
        fdaDataVerified = true
    )
    val mockItem2 = CosmeticItemDto(
        id = "2",
        name = "KoColor Luminescent C Serum",
        brand = "KoColor",
        shadeName = "Luminous Glow",
        colorHex = "#FFF8E7",
        thumbnailUrl = "",
        imageUrl = "",
        macroCategory = "PREP",
        microCategory = "SERUM",
        price = 28.0,
        notes = null,
        formulation = "LIQUID",
        chemistryBase = "WATER",
        finish = "RADIANT",
        coverage = "SHEER",
        temperature = "NEUTRAL",
        volume = "30ml",
        paoMonths = 6,
        expiryDate = null,
        instructions = null,
        ingredients = emptyList(),
        allergens = emptyList(),
        isVegan = true,
        isCrueltyFree = true,
        fdaDataVerified = true
    )

    MaterialTheme {
        PackPreviewScreen(
            uiState = PackPreviewUiState(
                items = listOf(mockItem1, mockItem2),
                groupedItems = mapOf("PREP" to listOf(mockItem1, mockItem2)),
                selectedIds = setOf("1")
            ),
            onToggleSelection = {},
            onToggleCollapse = {},
            onSelectCategoryAll = {},
            onClearCategory = {},
            onSelectAll = {},
            onDeselectAll = {},
            onWipeCollection = {},
            onImportSelected = {},
            onSearchQueryChanged = {},
            onToggleValueSort = {},
            onItemInfoClick = {},
            onDismissNotes = {},
            onBack = {}
        )
    }
}
