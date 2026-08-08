package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
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
    onImportSelected: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

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

    Scaffold(
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
                onImportSelected = onImportSelected
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF745E7A))
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
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
                                onToggle = { onToggleSelection(item.id) }
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

@Preview(showBackground = true)
@Composable
private fun PackPreviewScreenPreview() {
    MaterialTheme {
        PackPreviewScreen(
            uiState = PackPreviewUiState(
                items = listOf(
                    PackItem(id = "1", name = "KoColor Purifying Gel Cleanser", brand = "KoColor", shade = "Clear Crystal", hexColor = "#F4F6F0", thumbnailUrl = "", imageUrl = "", macroCategory = "PREP"),
                    PackItem(id = "2", name = "KoColor Luminescent C Serum", brand = "KoColor", shade = "Luminous Glow", hexColor = "#FFF8E7", thumbnailUrl = "", imageUrl = "", macroCategory = "PREP")
                ),
                groupedItems = mapOf(
                    "PREP" to listOf(
                        PackItem(id = "1", name = "KoColor Purifying Gel Cleanser", brand = "KoColor", shade = "Clear Crystal", hexColor = "#F4F6F0", thumbnailUrl = "", imageUrl = "", macroCategory = "PREP"),
                        PackItem(id = "2", name = "KoColor Luminescent C Serum", brand = "KoColor", shade = "Luminous Glow", hexColor = "#FFF8E7", thumbnailUrl = "", imageUrl = "", macroCategory = "PREP")
                    )
                ),
                selectedIds = setOf("1")
            ),
            onToggleSelection = {},
            onToggleCollapse = {},
            onSelectCategoryAll = {},
            onClearCategory = {},
            onSelectAll = {},
            onDeselectAll = {},
            onImportSelected = {},
            onBack = {}
        )
    }
}
