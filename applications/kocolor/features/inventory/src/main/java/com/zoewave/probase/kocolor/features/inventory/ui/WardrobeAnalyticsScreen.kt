package com.zoewave.probase.kocolor.features.inventory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.inventory.R
import com.zoewave.probase.kocolor.features.inventory.ui.components.AnalyticsStatCard
import com.zoewave.probase.kocolor.features.inventory.ui.components.AnalyticsStatUiState
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeEfficiencyRow
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeEfficiencyUiState
import com.zoewave.probase.kocolor.features.inventory.ui.components.WardrobeTaxonomyDialog
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeAnalyticsScreen(
    uiState: WardrobeUiState,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    if (showTaxonomyInfo) {
        WardrobeTaxonomyDialog(
            uiState = Unit,
            onEvent = { showTaxonomyInfo = false },
            navTo = {}
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_inventory_style_intelligence), style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_inventory_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showTaxonomyInfo = true }) {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_inventory_info_icon),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_style_dna),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_inventory_style_dna_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_portfolio_performance), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        AnalyticsStatCard(
                            uiState = AnalyticsStatUiState(
                                label = stringResource(R.string.applications_kocolor_features_inventory_total_value), 
                                value = currencyFormatter.format(uiState.totalInvestment), 
                                icon = Icons.Default.MonetizationOn
                            ),
                            modifier = Modifier.weight(1f),
                            onEvent = {}
                        )
                        AnalyticsStatCard(
                            uiState = AnalyticsStatUiState(
                                label = stringResource(R.string.applications_kocolor_features_inventory_avg_cpw), 
                                value = uiState.items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) stringResource(R.string.applications_kocolor_features_inventory_not_available) else currencyFormatter.format(it.average()) }, 
                                icon = Icons.AutoMirrored.Filled.TrendingDown
                            ),
                            modifier = Modifier.weight(1f),
                            onEvent = {}
                        )
                    }
                }
            }

            // 4. Wardrobe Palette (Chromatic Core)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_wardrobe_palette), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    
                    val colorGroups = remember(uiState.items) {
                        uiState.items
                            .filter { it.colorHex.isNotBlank() }
                            .groupBy { it.colorHex }
                            .toList()
                            .sortedWith(compareBy(
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                        // 🛠️ SPECTRAL HIERARCHY: Move neutrals to the end
                                        if (hsv[1] < 0.1f) 1 else 0 
                                    } catch (e: Exception) { 1 }
                                },
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                        hsv[0] // 2. Hue
                                    } catch (e: Exception) { 0f }
                                },
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                        hsv[2] // 3. Value
                                    } catch (e: Exception) { 0f }
                                }
                            ))
                    }

                    var selectedGroup by remember { mutableStateOf<Pair<String, List<ClothingItem>>?>(null) }

                    if (colorGroups.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_palette_prompt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // The Spectrum Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                colorGroups.forEach { group ->
                                    val (hex, items) = group
                                    val isSelected = selectedGroup?.first == hex
                                    Box(
                                        modifier = Modifier
                                            .weight(items.size.toFloat())
                                            .fillMaxHeight()
                                            .background(parseColor(hex))
                                            .border(
                                                width = if (isSelected) 3.dp else 0.dp,
                                                color = if (isSelected) Color.White else Color.Transparent
                                            )
                                            .clickable { 
                                                selectedGroup = if (isSelected) null else group 
                                            }
                                    )
                                }
                            }
                            
                            // 🔍 Selection Details
                            selectedGroup?.let { (hex, items) ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(parseColor(hex))
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = "${items.size} ${if (items.size == 1) "Garment" else "Garments"} in this shade",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    items.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { navTo(KoColorRoute.WardrobeDetail(item.internalId)) }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                Text(item.brand ?: "Unknown Brand", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp).rotate(180f),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_inventory_style_efficiency), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val bestValue = remember(uiState.items) {
                        uiState.items.filter { it.costPerUse != null }.sortedBy { it.costPerUse }.take(5)
                    }
                    
                    if (bestValue.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_inventory_efficiency_prompt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            bestValue.forEach { item ->
                                WardrobeEfficiencyRow(
                                    uiState = WardrobeEfficiencyUiState(
                                        item = item, 
                                        label = stringResource(R.string.applications_kocolor_features_inventory_per_wear)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WardrobeAnalyticsScreenPreview() {
    MaterialTheme {
        WardrobeAnalyticsScreen(
            uiState = WardrobeUiState(
                totalInvestment = 2450.0,
                items = listOf(
                    ClothingItem(internalId = 1, name = "Silk Blazer", category = ClothingCategory.TOPS, usageCount = 12, colorHex = "#F5F5DC", price = 350.0),
                    ClothingItem(internalId = 2, name = "Denim Jeans", category = ClothingCategory.BOTTOMS, usageCount = 45, colorHex = "#000080", price = 120.0)
                )
            ),
            navTo = {}
        )
    }
}
