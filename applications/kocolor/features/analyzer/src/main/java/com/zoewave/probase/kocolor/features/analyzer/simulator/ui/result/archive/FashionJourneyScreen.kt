package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        text = "THE JOURNEY",
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
                        text = "A Curated Editorial",
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
                        AtmospherePill("Vibe", model.circadianContext.split(" ").firstOrNull() ?: "Defense")
                    }
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Why: ${model.anchorReason.replace(Regex("\\[.*?\\]"), "").trim()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic
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
                    title = "The Architect's Rationale",
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
                            Text(
                                text = model.aiRationale.replace(Regex("(?i)feature\\s+\\d+\\s+is\\s+not\\s+available.*"), "").trim(),
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
