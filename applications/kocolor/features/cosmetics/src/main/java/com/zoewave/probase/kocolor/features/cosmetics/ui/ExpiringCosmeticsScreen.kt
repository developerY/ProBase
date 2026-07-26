package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.CosmeticProductCard
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.kocolor.model.KoColorRoute
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory

@Preview(showBackground = true)
@Composable
private fun ExpiringCosmeticsScreenPreview() {
    MaterialTheme {
        ExpiringCosmeticsScreen(
            uiState = CosmeticsUiState(
                items = listOf(
                    CosmeticItem(
                        name = "Sample", 
                        brand = "Brand", 
                        macroCategory = MacroCategory.COMPLEXION, 
                        microCategory = MicroCategory.FOUNDATION,
                        colorHex = "#FFFFFF",
                        expiryDate = System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiringCosmeticsScreen(
    uiState: CosmeticsUiState,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    
    val expiringItems = remember(uiState.items) {
        uiState.items.filter { item ->
            item.estimatedExpiry?.let { expiry ->
                (expiry - now) in 0..thirtyDaysInMillis
            } ?: false
        }.sortedBy { it.estimatedExpiry }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_expiring_soon), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                }
            )
        }
    ) { padding ->
        if (expiringItems.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_no_usage_data), style = MaterialTheme.typography.bodyLarge) 
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_expiring_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(expiringItems) { item ->
                    CosmeticProductCard(
                        uiState = item,
                        onEvent = onEvent,
                        navTo = navTo
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
