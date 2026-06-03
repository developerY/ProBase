package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.InventoryProductCard
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.MacroCategory

@Preview(showBackground = true)
@Composable
private fun InventoryManagementScreenPreview() {
    MaterialTheme {
        InventoryManagementScreen(
            uiState = CosmeticsUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryManagementScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val atelierBrown = Color(0xFF8B5E3C)
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navTo(KoColorRoute.CosmeticAdd()) },
                containerColor = atelierBrown,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_add_item))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFBF8F5)) 
        ) {
            HeaderSection(uiState.totalCosmetics)
            
            SearchBarAndFilter(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(CosmeticsEvent.UpdateSearchQuery(it)) }
            )
            
            val categories = remember { listOf("All Products") + MacroCategory.entries.map { it.displayName } }
            var selectedCategory by remember { mutableStateOf("All Products") }
            
            val displayItems = remember(uiState.filteredItems, selectedCategory) {
                if (selectedCategory == "All Products") {
                    uiState.filteredItems
                } else {
                    uiState.filteredItems.filter { it.macroCategory.displayName == selectedCategory }
                }
            }

            CategoryChipsSection(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            InventoryList(
                items = displayItems,
                onItemClick = { navTo(KoColorRoute.CosmeticDetail(it.id)) }
            )
        }
    }
}

@Composable
private fun HeaderSection(totalCount: Int) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Inventory Management",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            color = Color(0xFF2C2420)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Manage and track your $totalCount cosmetic variants.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchBarAndFilter(
    query: String,
    onQueryChange: (String) -> Unit
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
                    stringResource(R.string.applications_kocolor_features_cosmetics_search_placeholder), 
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
            onClick = { /* TODO */ },
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
            Text(stringResource(R.string.applications_kocolor_features_cosmetics_filter), color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun CategoryChipsSection(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
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

@Composable
private fun InventoryList(
    items: List<CosmeticItem>,
    onItemClick: (CosmeticItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            InventoryProductCard(item = item, onClick = { onItemClick(item) })
        }
    }
}
