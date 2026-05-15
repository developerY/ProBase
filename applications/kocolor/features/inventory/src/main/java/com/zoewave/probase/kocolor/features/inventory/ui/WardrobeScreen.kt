package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun WardrobeRoute(
    uiState: Unit = Unit,
    onEvent: (Unit) -> Unit = {},
    navTo: (KoColorRoute) -> Unit
) {
    val viewModel: WardrobeViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    WardrobeScreen(
        uiState = state,
        onEvent = viewModel::onEvent,
        navTo = navTo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    uiState: WardrobeUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wardrobe Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                // In a real app, this would open a dialog to add an item
                onEvent(WardrobeEvent.AddItem(
                    ClothingItem(
                        name = "New Item",
                        brand = "Brand",
                        category = ClothingCategory.TOPS,
                        colorHex = "#000000"
                    )
                ))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Your wardrobe is empty.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items) { item ->
                    WardrobeCard(
                        uiState = item,
                        onEvent = { onEvent(WardrobeEvent.DeleteItem(item.id)) },
                        navTo = navTo
                    )
                }
            }
        }
    }
}

@Composable
fun WardrobeCard(
    uiState: ClothingItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colorHex = uiState.colorHex
            if (colorHex != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = uiState.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${uiState.category.name} • ${uiState.brand ?: "Unknown Brand"}", style = MaterialTheme.typography.bodyMedium)
                if (!uiState.size.isNullOrEmpty()) {
                    Text(text = "Size: ${uiState.size}", style = MaterialTheme.typography.bodySmall)
                }
            }

            IconButton(onClick = { onEvent(Unit) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeCardPreview() {
    MaterialTheme {
        WardrobeCard(
            uiState = ClothingItem(name = "T-Shirt", brand = "Sample", category = ClothingCategory.TOPS, colorHex = "#FF0000"),
            onEvent = {},
            navTo = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeScreenPreview() {
    MaterialTheme {
        WardrobeScreen(
            uiState = WardrobeUiState(
                items = listOf(ClothingItem(name = "T-Shirt", category = ClothingCategory.TOPS)),
                isLoading = false
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
