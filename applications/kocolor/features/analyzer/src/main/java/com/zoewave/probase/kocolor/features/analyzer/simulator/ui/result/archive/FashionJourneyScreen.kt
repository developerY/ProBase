package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditStep
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FashionJourneyScreen(
    model: StyleCreationUiModel,
    modifier: Modifier = Modifier,
    navTo: (KoColorRoute) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_analyzer_story_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header / Intro
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_analyzer_story_subtitle).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "The Making of Your Look",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.width(48.dp), thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Chapter 1: The Vision
            item {
                EditorialChapter(
                    chapterNumber = "01",
                    title = "The Vision & Atmosphere",
                    icon = Icons.Outlined.WbSunny
                ) {
                    if (!model.userIntent.isNullOrBlank()) {
                        Text(
                            text = "\"${model.userIntent}\"",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(
                        text = "Designed for a ${model.occasion} under ${model.appearanceTemperature.lowercase()} and ${model.appearanceDepth.lowercase()} atmospheric conditions.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AtmospherePill("Temp", "${model.temperatureC ?: "--"}°C")
                        AtmospherePill("UV", "${model.uvIndex ?: "--"}")
                        AtmospherePill("Circadian", model.circadianContext.split(" ").firstOrNull() ?: "Defense")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "${model.clothing.size + model.cosmetics.size * 3} wardrobe items survived deterministic weather & climate gating.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Chapter 2: The Foundation
            item {
                EditorialChapter(
                    chapterNumber = "02",
                    title = "The Foundational Anchor",
                    icon = Icons.Outlined.Checkroom
                ) {
                    Text(
                        text = "Every compelling outfit begins with a grounding piece. KoColor's deterministic engine isolated the perfect anchor:",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = model.anchorName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your request called for a specific style profile, so KoColor promoted a high-confidence candidate as the foundational anchor.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Chapter 3: The Assembly
            item {
                EditorialChapter(
                    chapterNumber = "03",
                    title = "The Complete Ensemble",
                    icon = Icons.Outlined.Brush
                ) {
                    Text(
                        text = "With the anchor set, the remaining pieces were selected to build a cohesive silhouette and color story.",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("WARDROBE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    model.clothing.forEach { item ->
                        EditorialItemRow(item)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("BEAUTY LAYER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    model.cosmetics.forEach { item ->
                        EditorialItemRow(item)
                    }
                }
            }

            // Chapter 4: The Architect's Note
            item {
                EditorialChapter(
                    chapterNumber = "04",
                    title = stringResource(R.string.applications_kocolor_features_analyzer_architects_rationale),
                    icon = Icons.Outlined.AutoAwesome
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            var filteredRationale = model.aiRationale.removePrefix("Local Architect: ").replace(Regex("(?i)feature\\s+\\d+\\s+is\\s+not\\s+available.*"), "").trim()
                            
                            // 1. Replace Clothing items (w_##) with their actual names
                            model.clothing.forEach { clothingItem ->
                                val idStr = clothingItem.id
                                filteredRationale = filteredRationale.replace(Regex("(?i)item\\s+$idStr"), clothingItem.name.lowercase())
                                filteredRationale = filteredRationale.replace("<ITEM:$idStr>", clothingItem.name.lowercase())
                                filteredRationale = filteredRationale.replace("[$idStr]", clothingItem.name.lowercase())
                                filteredRationale = filteredRationale.replace(idStr, clothingItem.name.lowercase())
                            }
                            
                            // 2. Replace Cosmetic items (c_##) with their actual names
                            model.cosmetics.forEach { makeupItem ->
                                val idStr = makeupItem.id
                                val name = makeupItem.name.ifBlank { "this cosmetic" }
                                filteredRationale = filteredRationale.replace(Regex("(?i)item\\s+$idStr"), name.lowercase())
                                filteredRationale = filteredRationale.replace("<ITEM:$idStr>", name.lowercase())
                                filteredRationale = filteredRationale.replace("[$idStr]", name.lowercase())
                                filteredRationale = filteredRationale.replace(idStr, name.lowercase())
                            }
                            
                            // 3. Fallback
                            filteredRationale = filteredRationale
                                .replace(Regex("(?i)item\\s+w_\\d+"), "this garment")
                                .replace(Regex("(?i)item\\s+c_\\d+"), "this product")
                                .replace(Regex("<ITEM:w_\\d+>"), "this garment")
                                .replace(Regex("<ITEM:c_\\d+>"), "this product")
                                .replace(Regex("\\[w_\\d+\\]"), "this garment")
                                .replace(Regex("\\[c_\\d+\\]"), "this product")
                                .replace(Regex("w_\\d+"), "this garment")
                                .replace(Regex("c_\\d+"), "this product")

                            Text(
                                text = filteredRationale,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Chapter 5: The Harmony
            // Collapsible Audit Trail Section
            item {
                var auditExpanded by remember { mutableStateOf(false) }
                val rotation by animateFloatAsState(targetValue = if (auditExpanded) 180f else 0f, label = "AuditChevron")
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { auditExpanded = !auditExpanded },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.animateContentSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KOCOLOR AUDIT TRAIL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand Audit Trail",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                        
                        if (auditExpanded) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (model.rawAuditLog != null) {
                                    Text(
                                        text = model.rawAuditLog.trimIndent(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.DarkGray,
                                        lineHeight = 14.sp
                                    )
                                } else {
                                    model.auditSteps.forEachIndexed { index, step ->
                                        Column {
                                            Text(
                                                text = "[${index + 1}] ${step.title.uppercase()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.DarkGray
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = step.details,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Chapter 5: The Harmony
            item {
                EditorialChapter(
                    chapterNumber = "05",
                    title = "Aesthetic Harmony",
                    icon = Icons.Outlined.Checkroom
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FASHIONISTA SCORE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${model.fashionistaScore.roundToInt()}",
                                style = MaterialTheme.typography.displayMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Color Harmony: ${model.colorHarmony.roundToInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(text = "Silhouette: ${model.silhouette.roundToInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(text = "Contrast: ${model.contrastDepth.roundToInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    if (model.paletteHex.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "SIGNATURE PALETTE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            model.paletteHex.forEach { hex ->
                                val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
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
private fun EditorialChapter(
    chapterNumber: String,
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = chapterNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(imageVector = icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        content()
    }
}

@Composable
private fun EditorialItemRow(item: StyleItemUiModel) {
    val color = try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (e: Exception) { Color.Gray }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${item.role} ${item.material?.let { "• $it" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun AtmospherePill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
private fun FashionJourneyScreenPreview() {
    MaterialTheme {
        FashionJourneyScreen(
            model = StyleCreationUiModel(
                userIntent = "A bold statement for the city.",
                occasion = "Evening Gala",
                appearanceTemperature = "Cool",
                appearanceDepth = "Deep",
                temperatureC = 18.5f,
                uvIndex = 1.2f,
                anchorName = "Midnight Velvet Blazer",
                anchorReason = "Selected for its high formality and contrast.",
                clothing = listOf(
                    StyleItemUiModel("1", "Midnight Velvet Blazer", "OUTERWEAR", "#0B0C10", material = "Silk Velvet"),
                    StyleItemUiModel("2", "Charcoal Silk Trousers", "BOTTOM", "#1F2833", material = "100% Silk")
                ),
                cosmetics = listOf(
                    StyleItemUiModel("3", "Crimson Matte", "LIP", "#DC143C")
                ),
                aiRationale = "The velvet blazer grounds the look in deep, cool tones perfectly suited for an evening gala, while the crimson lip provides a striking focal point.",
                paletteHex = listOf("#0B0C10", "#1F2833", "#C5C6C7", "#DC143C"),
                fashionistaScore = 96.5f,
                colorHarmony = 98f,
                silhouette = 94f,
                contrastDepth = 97f
            )
        )
    }
}


@Preview(
    showBackground = true,
    heightDp = 2700, // Increase this value to fit the entire scrollable content
    widthDp = 400
)
@Composable
private fun FashionJourneyScreenLongPreview() {
    MaterialTheme {
        FashionJourneyScreen(
            model = StyleCreationUiModel(
                userIntent = "A bold statement for the city.",
                occasion = "Evening Gala",
                appearanceTemperature = "Cool",
                appearanceDepth = "Deep",
                temperatureC = 18.5f,
                uvIndex = 1.2f,
                anchorName = "Midnight Velvet Blazer",
                anchorReason = "Selected for its high formality and contrast.",
                clothing = listOf(
                    StyleItemUiModel("1", "Midnight Velvet Blazer", "OUTERWEAR", "#0B0C10", material = "Silk Velvet"),
                    StyleItemUiModel("2", "Charcoal Silk Trousers", "BOTTOM", "#1F2833", material = "100% Silk")
                ),
                cosmetics = listOf(
                    StyleItemUiModel("3", "Crimson Matte", "LIP", "#DC143C")
                ),
                aiRationale = "The velvet blazer grounds the look in deep, cool tones perfectly suited for an evening gala, while the crimson lip provides a striking focal point.",
                paletteHex = listOf("#0B0C10", "#1F2833", "#C5C6C7", "#DC143C"),
                fashionistaScore = 96.5f,
                colorHarmony = 98f,
                silhouette = 94f,
                contrastDepth = 97f
            )
        )
    }
}
