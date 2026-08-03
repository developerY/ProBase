package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.cosmetics.ui.CosmeticDetailUiState
import com.zoewave.probase.core.model.ritual.CosmeticItem

@Composable
fun AtelierExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFF0F0F0))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isExpanded) }
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                fontWeight = FontWeight.Black,
                color = if (isExpanded) Color(0xFF8B5E3C) else Color(0xFF2C2420)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.padding(bottom = 24.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ApplicationGuideSection(item: CosmeticItem, atelierBrown: Color) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = item.instructions ?: stringResource(R.string.applications_kocolor_features_cosmetics_no_instructions),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 22.sp
        )
        item.notes?.let { notes ->
            if (notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFFBF8F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_personal_notes), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = atelierBrown)
                        Spacer(Modifier.height(8.dp))
                        Text(notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicalSafetySection(item: CosmeticItem) {
    val hasActiveRecall = item.fdaRecallStatus != null && 
                         item.fdaRecallStatus != "Clear" && 
                         item.fdaRecallStatus != "None"

    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when {
            !item.isFdaChecked -> {
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_fda_pending), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            hasActiveRecall -> {
                WarningBanner(
                    title = stringResource(R.string.applications_kocolor_features_cosmetics_active_fda_recall),
                    subtitle = stringResource(R.string.applications_kocolor_features_cosmetics_fda_recall_message_format, item.fdaRecallStatus ?: ""),
                    icon = Icons.Default.Warning
                )
            }
            item.fdaAdverseEventCount > 10 -> { // Threshold for "High Events"
                WarningBanner(
                    title = stringResource(R.string.applications_kocolor_features_cosmetics_adverse_events_reported_format, item.fdaAdverseEventCount),
                    subtitle = stringResource(R.string.applications_kocolor_features_cosmetics_reported_reactions_format, item.fdaTopReactions.take(3).joinToString(", ")),
                    icon = Icons.Default.Warning
                )
            }
            else -> {
                Surface(
                    color = Color(0xFFF0F7F0),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(20.dp), tint = Color(0xFF2E7D32))
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.applications_kocolor_features_cosmetics_fda_clean), style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        if (item.fdaActiveIngredients.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.applications_kocolor_features_cosmetics_otc_ingredients), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
            item.fdaActiveIngredients.forEach { ingredient ->
                DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_active_label), ingredient)
            }
        }

        if (item.fdaClinicalWarnings.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.applications_kocolor_features_cosmetics_fda_warnings), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
            item.fdaClinicalWarnings.take(2).forEach { warning ->
                Text(
                    text = stringResource(R.string.applications_kocolor_features_cosmetics_warning_bullet_format, warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun WarningBanner(title: String, subtitle: String, icon: ImageVector) {
    Surface(
        color = Color(0xFFFFF0F0),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color(0xFFD32F2F))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
fun IngredientAnalysisSection(item: CosmeticItem, bioSyncMessage: String?) {
    val resolvedHero = item.heroIngredient 
        ?: item.ingredients.firstOrNull()?.replaceFirstChar { it.uppercase() }
        ?: stringResource(R.string.applications_kocolor_features_cosmetics_analyzing)

    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_hero_ingredient), resolvedHero)
        
        bioSyncMessage?.let { msg ->
            Surface(
                color = Color(0xFFF0F7F0),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        } ?: DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_skin_compatibility), item.skinCompatibility ?: stringResource(R.string.applications_kocolor_features_cosmetics_universal))

        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_fragrance), if (item.containsFragrance == true) stringResource(R.string.applications_kocolor_features_cosmetics_contains_fragrance) else stringResource(R.string.applications_kocolor_features_cosmetics_none_unscented))
        
        if (item.allergens.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.applications_kocolor_features_cosmetics_allergen_alerts), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color(0xFFD32F2F))
            Surface(
                color = Color(0xFFFFF0F0),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp), tint = Color(0xFFD32F2F))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_cosmetics_contains_allergens_format, item.allergens.joinToString(", ")),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_no_allergens), style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
            }
        }

        if (item.ingredients.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.applications_kocolor_features_cosmetics_full_ingredients), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
            Surface(
                color = Color(0xFFFBF8F5),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.ingredients.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SustainabilitySection(item: CosmeticItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item.ecoScore?.let { score ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when(score) {
                        "A" -> Color(0xFF4CAF50)
                        "B" -> Color(0xFF8BC34A)
                        "C" -> Color(0xFFFFEB3B)
                        "D" -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(score, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_ecoscore_label, score), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Text(stringResource(R.string.applications_kocolor_features_cosmetics_eco_description), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_recyclable), if (item.recyclingInstructions != null) stringResource(R.string.applications_kocolor_features_cosmetics_yes) else stringResource(R.string.applications_kocolor_features_cosmetics_likely))
        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_vegan), if (item.isVegan == true) stringResource(R.string.applications_kocolor_features_cosmetics_certified) else if (item.isVegan == false) stringResource(R.string.applications_kocolor_features_cosmetics_no) else stringResource(R.string.applications_kocolor_features_cosmetics_likely))
        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_cruelty_free), if (item.isCrueltyFree == true) stringResource(R.string.applications_kocolor_features_cosmetics_yes) else stringResource(R.string.applications_kocolor_features_cosmetics_analyzing))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Recycling, stringResource(R.string.applications_kocolor_features_cosmetics_recyclable), tint = Color(0xFF4CAF50))
            if (item.isVegan != false) Icon(Icons.Default.Eco, stringResource(R.string.applications_kocolor_features_cosmetics_clean_beauty), tint = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun CoordinationSection(uvIndex: Double = 0.0) {
    val isHighUv = uvIndex > 3.0
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.applications_kocolor_features_cosmetics_works_well_with), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val products = mutableListOf("Silk Primer", "Radiance Mist")
            if (isHighUv) products.add(0, "Daily SPF 50+") else products.add("Daily SPF")
            
            products.forEach { product ->
                val isElevated = isHighUv && product.contains("SPF 50+")
                Surface(
                    color = if (isElevated) MaterialTheme.colorScheme.primaryContainer else Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isElevated) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0))
                ) {
                    Text(
                        text = product,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isElevated) MaterialTheme.colorScheme.onPrimaryContainer else Color.Black
                    )
                }
            }
        }
        
        if (isHighUv) {
            Text(
                text = "☀️ UV Index is high. Elevated SPF protection is recommended.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
fun ValueAnalysisSection(item: CosmeticItem) {
    val remainingValue = (item.fillLevel ?: 1.0) * (item.price ?: 0.0)
    Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_retail_investment), "$%.2f".format(item.price ?: 0.0))
        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_actual_cost_per_use), item.costPerUse?.let { "$%.2f".format(it) } ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available))
        DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_remaining_value), "$%.2f".format(remainingValue), valueColor = Color(0xFF8B5E3C))
    }
}

@Composable
fun ColorHueMapSection(colorHex: String, shadeName: String?, compatibility: List<String>) {
    val color = parseColor(colorHex)
    val hsv = remember(colorHex) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(colorHex), hsv)
        hsv
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_cosmetics_color_hue_map),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
            )
            
            Spacer(Modifier.width(20.dp))
            
            Column {
                Text(text = shadeName ?: stringResource(R.string.applications_kocolor_features_cosmetics_custom_tone), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.applications_kocolor_features_cosmetics_hue_sat_format, hsv[0].toInt(), (hsv[1] * 100).toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Hue Spectrum
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                    )
                )
        ) {
            val bias = hsv[0] / 360f
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (bias * 320).dp) 
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(stringResource(R.string.applications_kocolor_features_cosmetics_coordination_palette), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            compatibility.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(parseColor(hex))
                        .border(0.5.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
fun ProfessionalFacetsSection(item: CosmeticItem) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_cosmetics_professional_facets),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_formulation), item.formulation.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_chemistry_base), item.chemistryBase.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_finish), item.finish.name.lowercase().replaceFirstChar { it.uppercase() })
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_coverage), item.coverage.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() })
        }
    }
}

@Composable
fun UsageStockSection(item: CosmeticItem, uiState: CosmeticDetailUiState) {
    val fillLevel = item.fillLevel ?: 1.0
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_cosmetics_usage_stock_health),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_total_uses), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${item.usageCount}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_est_days_left), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${uiState.estimatedDaysRemaining ?: stringResource(R.string.applications_kocolor_features_cosmetics_not_available)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        ShareProgressBar(stringResource(R.string.applications_kocolor_features_cosmetics_stock_remaining), fillLevel)
        
        if (fillLevel < 0.2) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { /* Add to Shopping List */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.applications_kocolor_features_cosmetics_add_to_shopping_list), fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = stringResource(R.string.applications_kocolor_features_cosmetics_usage_frequency_desc_format, uiState.usageFrequencyPerWeek),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ProductDatesSection(item: CosmeticItem) {
    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    val notSet = stringResource(R.string.applications_kocolor_features_cosmetics_not_set)
    fun formatLong(timestamp: Long?): String = timestamp?.let { dateFormatter.format(java.util.Date(it)) } ?: notSet

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_cosmetics_product_lifecycle),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2420)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_added_to_collection), formatLong(item.timestamp))
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_first_opened), formatLong(item.openedDate))
            DetailMetricRow(stringResource(R.string.applications_kocolor_features_cosmetics_estimated_expiry), formatLong(item.estimatedExpiry), valueColor = if ((item.estimatedExpiry ?: Long.MAX_VALUE) < System.currentTimeMillis()) Color.Red else Color.Black)
        }
    }
}

@Composable
fun DetailMetricRow(label: String, value: String, valueColor: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light, fontFamily = FontFamily.Serif, color = valueColor)
    }
}

@Composable
fun ShareProgressBar(label: String, share: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(stringResource(R.string.applications_kocolor_features_cosmetics_stock_remaining_format, share * 100), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { share.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = Color(0xFF8B5E3C),
            trackColor = Color(0xFFF0F0F0)
        )
    }
}
