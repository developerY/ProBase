package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeCard
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
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
                items = listOf(ClothingItem(name = "T-Shirt", category = ClothingCategory.TOPS)),
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
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_inventory_wardrobe_title), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Clothing")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.applications_kocolor_features_inventory_empty_collection), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val filteredItems = uiState.items

            val groupedItems = remember(filteredItems) {
                filteredItems.groupBy { it.category }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                groupedItems.forEach { (category, items) ->
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_inventory_curated_pieces_format, items.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            maxItemsInEachRow = 2
                        ) {
                            items.forEach { item ->
                                WardrobeCard(
                                    uiState = item,
                                    onEvent = { onEvent(WardrobeEvent.DeleteItem(item.id)) },
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
