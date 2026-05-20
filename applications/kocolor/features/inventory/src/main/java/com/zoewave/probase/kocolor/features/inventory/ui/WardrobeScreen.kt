package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.kocolor.features.inventory.util.toComposeColor
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

@Preview(showBackground = true)
@Composable
private fun WardrobeRoutePreview() {
    MaterialTheme {
        WardrobeRoute(
            uiState = WardrobeUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}

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
                title = { Text("The Wardrobe", style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navTo(KoColorRoute.WardrobeColorVerification) }) {
                        Icon(Icons.Default.Palette, contentDescription = "Test Colors")
                    }
                    IconButton(onClick = { 
                        // We could add a proper "Add" route later
                        onEvent(WardrobeEvent.AddItem(
                            ClothingItem(
                                name = "New Essential",
                                category = ClothingCategory.TOPS,
                                colorHex = "#FFFFFF"
                            )
                        ))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Your collection is ready to be curated.", style = MaterialTheme.typography.bodyLarge)
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
                                text = "${items.size} curated pieces",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    item {
                        // Staggered-style row behavior within lazy column for editorial feel
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

@Composable
fun WardrobeCard(
    uiState: ClothingItem,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = uiState.colorHex.toComposeColor()
    val isDark = isColorDark(bgColor)
    val contentColor = if (isDark) Color.White else Color.Black

    ElevatedCard(
        modifier = modifier.clickable { navTo(KoColorRoute.WardrobeDetail(uiState.id)) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.imageUrl != null) {
                coil.compose.AsyncImage(
                    model = uiState.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Checkroom,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).align(Alignment.Center).alpha(0.1f),
                    tint = contentColor
                )
            }

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = uiState.brand?.uppercase() ?: "KOCOLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = uiState.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
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
