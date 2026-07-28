package com.zoewave.probase.kocolor.features.cosmetics.ui

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
import androidx.compose.material.icons.filled.History
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
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.AnalyticsStatCard
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.AnalyticsStatUiState
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.ProfessionalTaxonomyDialog
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.UsageRankingRow
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.UsageRankingUiState
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.ValueEfficiencyRow
import com.zoewave.probase.kocolor.features.cosmetics.ui.components.ValueEfficiencyUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmeticAnalyticsScreen(
    uiState: CosmeticsUiState,
    modifier: Modifier = Modifier,
    onEvent: (CosmeticsEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    var showTaxonomyInfo by remember { mutableStateOf(false) }

    if (showTaxonomyInfo) {
        ProfessionalTaxonomyDialog(
            onEvent = { showTaxonomyInfo = false },
            navTo = {}
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.applications_kocolor_features_cosmetics_analytics_title), style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp) },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_cosmetics_back))
                    }
                },
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Headline
            item {
                Column {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_beauty_blueprint),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_data_insights_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. High-Level Performance Metrics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_performance_label), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
                        AnalyticsStatCard(
                            uiState = AnalyticsStatUiState(
                                label = stringResource(R.string.applications_kocolor_features_cosmetics_total_uses), 
                                value = uiState.items.sumOf { it.usageCount }.toString(), 
                                icon = Icons.Default.History
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsStatCard(
                            uiState = AnalyticsStatUiState(
                                label = stringResource(R.string.applications_kocolor_features_cosmetics_avg_cpu), 
                                value = uiState.items.mapNotNull { it.costPerUse }.let { if (it.isEmpty()) stringResource(R.string.applications_kocolor_features_cosmetics_not_available) else currencyFormatter.format(it.average()) }, 
                                icon = Icons.AutoMirrored.Filled.TrendingDown
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Usage Leaderboard
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_usage_leaderboard), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val topUsed = uiState.items.filter { it.usageCount > 0 }.sortedByDescending { it.usageCount }.take(5)
                    
                    if (topUsed.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_no_usage_data), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topUsed.forEachIndexed { index, item ->
                                UsageRankingRow(
                                    uiState = UsageRankingUiState(
                                        item = item, 
                                        rank = index + 1, 
                                        maxUsage = topUsed.first().usageCount
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Chromatic Core
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_chromatic_core), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    
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
                                        hsv[0] // 1. Hue (Rainbow order)
                                    } catch (e: Exception) { 0f }
                                },
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                        hsv[1] // 2. Saturation (Muted to Vibrant)
                                    } catch (e: Exception) { 0f }
                                },
                                { (hex, _) ->
                                    val hsv = FloatArray(3)
                                    try {
                                        AndroidColor.colorToHSV(AndroidColor.parseColor(hex), hsv)
                                        hsv[2] // 3. Value (Dark to Light)
                                    } catch (e: Exception) { 0f }
                                }
                            ))
                    }

                    var selectedGroup by remember { mutableStateOf<Pair<String, List<CosmeticItem>>?>(null) }

                    if (colorGroups.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_capture_colors_prompt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            
                            // 🔍 Selection Details (What item adds what color)
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
                                            text = "${items.size} ${if (items.size == 1) "Product" else "Products"} in this shade",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    items.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { navTo(KoColorRoute.CosmeticDetail(item.id)) }
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                Text(item.brand, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            // 5. Efficiency Analysis
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_style_efficiency), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    val bestValue = uiState.items.filter { it.costPerUse != null }.sortedBy { it.costPerUse }.take(3)
                    
                    if (bestValue.isEmpty()) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_complete_cycles_prompt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            bestValue.forEach { item ->
                                ValueEfficiencyRow(
                                    uiState = ValueEfficiencyUiState(
                                        item = item, 
                                        label = stringResource(R.string.applications_kocolor_features_cosmetics_per_use)
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
private fun CosmeticAnalyticsScreenPreview() {
    MaterialTheme {
        CosmeticAnalyticsScreen(
            uiState = CosmeticsUiState(
                items = listOf(
                    CosmeticItem(
                        id = 1, 
                        name = "Silk Primer", 
                        brand = "KoColor", 
                        macroCategory = MacroCategory.PREP,
                        microCategory = MicroCategory.PRIMER,
                        usageCount = 45, 
                        colorHex = "#F8F0E3", 
                        price = 28.0
                    ),
                    CosmeticItem(
                        id = 2, 
                        name = "Cool Ivory", 
                        brand = "KoColor", 
                        macroCategory = MacroCategory.COMPLEXION,
                        microCategory = MicroCategory.FOUNDATION,
                        usageCount = 120, 
                        colorHex = "#FAD4D4", 
                        price = 42.0
                    )
                )
            ),
            onEvent = {},
            navTo = {}
        )
    }
}
