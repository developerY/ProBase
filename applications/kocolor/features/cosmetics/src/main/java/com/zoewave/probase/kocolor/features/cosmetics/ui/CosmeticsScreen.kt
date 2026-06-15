package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.CosmeticProductCard
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.GroupSectionCard
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.SubCategoryCard
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.model.KoColorRoute

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

@Preview(showBackground = true)
@Composable
private fun CosmeticsScreenPreview() {
    MaterialTheme {
        CosmeticsScreen(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
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
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_inventory_title), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.CosmeticAdd()) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add))
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_sort))
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add_item))
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
                placeholder = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_search_placeholder)) },
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
