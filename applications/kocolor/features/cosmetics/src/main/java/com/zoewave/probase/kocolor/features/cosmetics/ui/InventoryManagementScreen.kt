package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.model.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.kocolor.model.MacroCategory
import java.text.NumberFormat
import java.util.Locale

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
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        },
        bottomBar = {
            MockBottomNavigation(atelierBrown)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFBF8F5)) // Light cream/atelier background
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
                    "Search by product name, SKU, or shade..", 
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
            Text("Filter", color = Color.DarkGray, fontSize = 14.sp)
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

@Composable
private fun InventoryProductCard(
    item: CosmeticItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (item.imageUrl != null) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        tint = Color.LightGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Metadata Header
                Text(
                    text = "${item.macroCategory.displayName.uppercase()} • ${item.batchCode ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2420),
                        modifier = Modifier.weight(1f)
                    )
                    
                    StockStatusBadge(item.fillLevel ?: 0.0)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FooterDetail(item)
                    
                    Text(
                        text = item.price?.let { 
                            NumberFormat.getCurrencyInstance(Locale.US).format(it)
                        } ?: "$0.00",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2420)
                    )
                }
            }
        }
    }
}

@Composable
private fun StockStatusBadge(fillLevel: Double) {
    val (text, color) = when {
        fillLevel > 0.5 -> "In Stock" to Color(0xFF81C784)
        fillLevel > 0.1 -> "Low Stock" to Color(0xFFE57373)
        else -> "Out of Stock" to Color(0xFFBDBDBD)
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FooterDetail(item: CosmeticItem) {
    val colorHex = item.colorHex
    if (colorHex != null || item.shadeName != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Shade: ${item.shadeName ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (colorHex != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                        .border(0.5.dp, Color.LightGray, CircleShape)
                )
            }
        }
    } else {
        Text(
            text = "Size: ${item.volume ?: "N/A"}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun MockBottomNavigation(activeColor: Color) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            "Dashboard" to Icons.Default.Dashboard,
            "Inventory" to Icons.Default.Inventory,
            "Analytics" to Icons.Default.Analytics,
            "Profile" to Icons.Default.Person
        )
        
        items.forEach { (label, icon) ->
            NavigationBarItem(
                selected = label == "Inventory",
                onClick = { },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}


