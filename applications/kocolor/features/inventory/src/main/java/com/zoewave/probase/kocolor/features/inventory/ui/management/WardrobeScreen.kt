package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeCard
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun WardrobeRoute(
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    WardrobeScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Preview(showBackground = true)
@Composable
private fun WardrobeScreenPreview() {
    MaterialTheme {
        WardrobeScreen(
            uiState = WardrobeUiState(
                items = listOf(ClothingItem(name = "T-Shirt", category = ClothingCategory.TOPS, colorHex = "#FFFFFF")),
                isLoading = false
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val darkPlum = Color(0xFF5A3854)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wardrobe Inventory", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.WardrobeColorVerification) }) {
                        Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_test_colors))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.ClothingCapture) },
                containerColor = darkPlum,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Clothing")
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFBF8F5))
        ) {
            HeaderSection(
                totalCount = uiState.totalItems
            )
            
            SearchBarAndFilter(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(WardrobeEvent.UpdateSearchQuery(it)) },
                onFilterClick = { /* Filter */ }
            )
            
            val allPiecesLabel = "All Pieces"
            val categories = remember { listOf(allPiecesLabel) + ClothingCategory.entries.map { it.displayName } }
            var selectedCategory by remember { mutableStateOf(allPiecesLabel) }
            
            val displayItems = remember(uiState.filteredItems, selectedCategory) {
                if (selectedCategory == allPiecesLabel) {
                    uiState.filteredItems
                } else {
                    uiState.filteredItems.filter { it.category.displayName == selectedCategory }
                }
            }

            CategoryChipsSection(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )
            
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (displayItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) "No matching wardrobe pieces found." else stringResource(R.string.applications_kocolor_features_inventory_empty_collection),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            maxItemsInEachRow = 2
                        ) {
                            displayItems.forEach { item ->
                                WardrobeCard(
                                    uiState = item,
                                    onEvent = { onEvent(WardrobeEvent.DeleteItem(item.internalId)) },
                                    navTo = navTo,
                                    modifier = Modifier.weight(1f).aspectRatio(0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(totalCount: Int) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Wardrobe Inventory",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            color = Color(0xFF2C2420)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$totalCount curated pieces in your archive",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchBarAndFilter(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 52.dp),
            placeholder = { 
                Text(
                    "Search wardrobe by brand, name...", 
                    fontSize = 13.sp,
                    color = Color.LightGray
                ) 
            },
            leadingIcon = { 
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null, 
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                ) 
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color(0xFFE0E0E0),
                unfocusedIndicatorColor = Color(0xFFE0E0E0),
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        OutlinedButton(
            onClick = onFilterClick,
            modifier = Modifier.height(52.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Icon(
                Icons.Default.FilterList, 
                contentDescription = null, 
                modifier = Modifier.size(18.dp),
                tint = Color.DarkGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Filter", color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CategoryChipsSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(category) },
                label = { Text(category) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF2C2420),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color.Gray
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFFE0E0E0),
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 0.dp
                )
            )
        }
    }
}
