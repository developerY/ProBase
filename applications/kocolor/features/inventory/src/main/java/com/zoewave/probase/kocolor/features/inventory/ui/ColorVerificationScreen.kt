package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.ColorVerificationItem
import com.zoewave.probase.kocolor.model.ClothingCategory
import com.zoewave.probase.kocolor.model.ClothingItem
import com.zoewave.probase.kocolor.model.KoColorRoute

data class ColorVerificationUiState(
    val items: List<ClothingItem> = emptyList()
)

@Composable
fun ColorVerificationRoute(
    uiState: ColorVerificationUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    ColorVerificationScreen(
        uiState = uiState,
        onEvent = onEvent,
        navTo = navTo
    )
}

@Preview(showBackground = true)
@Composable
private fun ColorVerificationScreenPreview() {
    MaterialTheme {
        ColorVerificationScreen(
            uiState = ColorVerificationUiState(
                items = listOf(ClothingItem(name = "Item", category = ClothingCategory.TOPS))
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorVerificationScreen(
    uiState: ColorVerificationUiState,
    onEvent: (WardrobeEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_inventory_test_colors)) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.items) { item ->
                ColorVerificationItem(uiState = item, onEvent = {}, navTo = {})
            }
        }
    }
}
