package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.data.usecase.CreationPhase
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditStep
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditTrailView
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ExecutionTier

data class StyleCreationUiModel(
    val occasion: String = "Daily Outfit",
    val userIntent: String? = null,
    val appearanceTemperature: String = "Neutral",
    val appearanceDepth: String = "Light",
    val appearanceContrast: String = "Balanced",
    val temperatureC: Float? = 23.4f,
    val uvIndex: Float? = 5.75f,
    val circadianContext: String = "Defense & Protection",
    val eligibleWardrobeCount: Int = 53,
    val anchorName: String = "Universal Khaki Button-Down",
    val anchorId: String = "w_41",
    val anchorReason: String = "Automatic context anchor",
    val clothing: List<StyleItemUiModel> = emptyList(),
    val cosmetics: List<StyleItemUiModel> = emptyList(),
    val aiRationale: String = "Selected from your vault based on intent and availability.",
    val validationItems: List<ValidationUiModel> = emptyList(),
    val paletteHex: List<String> = emptyList(),
    val fashionistaScore: Float = 85.4f,
    val colorHarmony: Float = 98.0f,
    val silhouette: Float = 75.0f,
    val contrastDepth: Float = 80.0f,
    val intentStatus: IntentUiStatus = IntentUiStatus.NOT_SPECIFIED,
    val observedColorfulness: Float? = 0.53f,
    val observedColorContrast: Float? = 0.50f,
    val executionTier: ExecutionTier = ExecutionTier.AI_CLOUD,
    val latencyMs: Long = 1290L,
    val auditSteps: List<AuditStep> = listOf(
        AuditStep(1, "Anchor Establishment", "Resolved outfit anchor via Intent/Context Engine."),
        AuditStep(2, "Deterministic Pruning", "Inventory evaluated and weather-gated."),
        AuditStep(3, "Mathematical Scoring", "Top candidates ranked by relational color harmony."),
        AuditStep(4, "AI Synthesis & Validation", "Synthesized blueprint validated across 4 cosmetic roles.")
    )
)

data class StyleItemUiModel(
    val id: String,
    val name: String,
    val role: String,
    val colorHex: String,
    val temperature: String? = null,
    val material: String? = null
)

data class ValidationUiModel(
    val label: String,
    val passed: Boolean
)

enum class IntentUiStatus {
    NOT_SPECIFIED,
    SPECIFIED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleCreationStoryScreen(
    model: StyleCreationUiModel,
    modifier: Modifier = Modifier,
    phase: CreationPhase = CreationPhase.COMPLETE
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Your Style",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "How KoColor created this look",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                ContextCard(model)
            }

            item {
                TimelineStep(
                    number = "01",
                    icon = Icons.Outlined.FilterAlt,
                    title = "Understood the context",
                    subtitle = "${model.eligibleWardrobeCount} wardrobe items survived deterministic filtering.",
                    body = buildString {
                        append("${model.occasion} occasion")
                        if (model.userIntent.isNullOrBlank()) {
                            append(" • No specific style preference")
                        } else {
                            append(" • \"${model.userIntent}\"")
                        }
                    }
                )
            }

            item {
                TimelineStep(
                    number = "02",
                    icon = Icons.Outlined.Style,
                    title = "Established the anchor",
                    subtitle = model.anchorName,
                    body = "${model.anchorReason} • ${model.anchorId}"
                )
            }

            if (phase == CreationPhase.AI_GENERATING) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "AI Style Synthesis in Progress...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                if (model.clothing.isNotEmpty()) {
                    item {
                        OutfitAssemblyCard(
                            clothing = model.clothing
                        )
                    }
                }

                if (model.cosmetics.isNotEmpty()) {
                    item {
                        CosmeticSelectionCard(
                            cosmetics = model.cosmetics
                        )
                    }
                }

                item {
                    TimelineStep(
                        number = "04",
                        icon = Icons.Outlined.AutoAwesome,
                        title = "AI style synthesis",
                        subtitle = "Cloud AI used for synthesis with grounded candidate set.",
                        body = model.aiRationale.replace(Regex("(?i)feature\\s+\\d+\\s+is\\s+not\\s+available.*"), "").trim()
                    )
                }

                if (model.validationItems.isNotEmpty()) {
                    item {
                        ValidationCard(
                            validations = model.validationItems
                        )
                    }
                }

                if (model.paletteHex.isNotEmpty()) {
                    item {
                        PaletteCard(
                            palette = model.paletteHex
                        )
                    }
                }

                item {
                    FashionistaCard(
                        score = model.fashionistaScore,
                        colorHarmony = model.colorHarmony,
                        silhouette = model.silhouette,
                        contrastDepth = model.contrastDepth
                    )
                }

                item {
                    StyleCharacterCard(model)
                }

                item {
                    AuditTrailView(
                        executionTier = model.executionTier,
                        latencyMs = model.latencyMs,
                        fashionistaScore = model.fashionistaScore,
                        steps = model.auditSteps
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ContextCard(
    model: StyleCreationUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "YOUR CONTEXT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = model.userIntent?.takeIf { it.isNotBlank() }
                    ?: "No specific style preference",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContextChip("Temperature", model.appearanceTemperature)
                ContextChip("Depth", model.appearanceDepth)
                ContextChip("Contrast", model.appearanceContrast)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "Temp",
                    value = model.temperatureC?.let {
                        "${"%.1f".format(it)}°C"
                    } ?: "—"
                )

                MetricPill(
                    label = "UV",
                    value = model.uvIndex?.let {
                        "%.2f".format(it)
                    } ?: "—"
                )
            }

            Text(
                text = model.circadianContext,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TimelineStep(
    number: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    body: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(14.dp))

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun OutfitAssemblyCard(
    clothing: List<StyleItemUiModel>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(
                title = "03",
                headline = "Built the outfit",
                icon = Icons.Outlined.Style
            )

            clothing.forEach { item ->
                OutfitItemRow(item)
            }
        }
    }
}

@Composable
private fun OutfitItemRow(
    item: StyleItemUiModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(parseHex(item.colorHex))
        )

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.role,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            item.material?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item.temperature?.let {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(it) }
            )
        }
    }
}

@Composable
private fun CosmeticSelectionCard(
    cosmetics: List<StyleItemUiModel>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "COSMETICS",
                headline = "Completed the beauty layer",
                icon = Icons.Outlined.ColorLens
            )

            cosmetics.forEach { item ->
                CosmeticRow(item)
            }
        }
    }
}

@Composable
private fun CosmeticRow(
    item: StyleItemUiModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(parseHex(item.colorHex))
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.role,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item.temperature?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ValidationCard(
    validations: List<ValidationUiModel>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionHeader(
                title = "05",
                headline = "Verified the recommendation",
                icon = Icons.Outlined.Verified
            )

            validations.forEach { validation ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (validation.passed)
                            Icons.Outlined.CheckCircle
                        else
                            Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = if (validation.passed)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(validation.label)
                }
            }
        }
    }
}

@Composable
private fun FashionistaCard(
    score: Float,
    colorHarmony: Float,
    silhouette: Float,
    contrastDepth: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FASHIONISTA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Aesthetic evaluation",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "%.1f".format(score),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "/ 100",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(18.dp))

            ScoreRow("Color Harmony", colorHarmony)
            ScoreRow("Silhouette", silhouette)
            ScoreRow("Contrast & Depth", contrastDepth)

            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(50.dp),
                tonalElevation = 3.dp
            ) {
                Text(
                    text = "APPROVED",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScoreRow(
    label: String,
    score: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(
                text = "%.0f".format(score),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StyleCharacterCard(
    model: StyleCreationUiModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "STYLE CHARACTER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            if (model.intentStatus == IntentUiStatus.NOT_SPECIFIED) {
                Text(
                    text = "No specific style preference was provided."
                )
            }

            model.observedColorfulness?.let {
                CharacterRow(
                    label = "Colorfulness",
                    value = it
                )
            }

            model.observedColorContrast?.let {
                CharacterRow(
                    label = "Color Contrast",
                    value = it
                )
            }
        }
    }
}

@Composable
private fun CharacterRow(
    label: String,
    value: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)

        Text(
            text = "${(value * 100).toInt()}",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PaletteCard(
    palette: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RECOMMENDED PALETTE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                palette.forEach { hex ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(parseHex(hex))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = hex,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    headline: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ContextChip(
    label: String,
    value: String
) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text("$label: $value")
        }
    )
}

@Composable
private fun MetricPill(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = value,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun parseHex(hex: String): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(Color.Gray)
}

@Preview(showBackground = true, name = "Style Creation Story Preview")
@Composable
private fun StyleCreationStoryScreenPreview() {
    MaterialTheme {
        StyleCreationStoryScreen(
            model = StyleCreationUiModel(
                occasion = "Daily Outfit",
                userIntent = "fun colorful outfit",
                appearanceTemperature = "Neutral",
                appearanceDepth = "Light",
                appearanceContrast = "Balanced",
                temperatureC = 23.4f,
                uvIndex = 5.75f,
                circadianContext = "Defense & Protection",
                eligibleWardrobeCount = 53,
                anchorName = "Electric Coral Cropped Hoodie",
                anchorId = "w_3",
                anchorReason = "[INTENT ANCHOR] High-chroma intent override",
                clothing = listOf(
                    StyleItemUiModel(id = "w_3", name = "Electric Coral Cropped Hoodie", role = "TOP", colorHex = "#FF5F1F", temperature = "WARM", material = "100% Organic Cotton"),
                    StyleItemUiModel(id = "w_35", name = "Warm Ivory Pleated Trousers", role = "BOTTOM", colorHex = "#EDD5B1", temperature = "NEUTRAL", material = "Cotton Blend"),
                    StyleItemUiModel(id = "w_48", name = "Camel Leather Boots", role = "SHOES", colorHex = "#BDA06A", temperature = "WARM", material = "Full Grain Leather")
                ),
                cosmetics = listOf(
                    StyleItemUiModel(id = "c_123", name = "Golden Hour Shimmer", role = "EYE", colorHex = "#FFD700", temperature = "NEUTRAL"),
                    StyleItemUiModel(id = "c_78", name = "Natural Peach Blush", role = "CHEEK", colorHex = "#FFA07A", temperature = "WARM"),
                    StyleItemUiModel(id = "c_114", name = "Warm Terracotta Lipstick", role = "LIP", colorHex = "#C75B39", temperature = "WARM"),
                    StyleItemUiModel(id = "c_133", name = "Cobalt Core Polish", role = "NAIL", colorHex = "#0047AB", temperature = "COOL")
                ),
                aiRationale = "Selected an energetic Electric Coral Cropped Hoodie anchored with warm neutral pleated trousers and camel boots for an elevated, vibrant daily look.",
                validationItems = listOf(
                    ValidationUiModel("Mandatory anchor included", true),
                    ValidationUiModel("Top / Bottom / Shoes composition", true),
                    ValidationUiModel("Eye / Cheek / Lip / Nail roles", true),
                    ValidationUiModel("All selected IDs grounded", true),
                    ValidationUiModel("Forbidden PREP items excluded", true)
                ),
                paletteHex = listOf("#FF5F1F", "#EDD5B1", "#BDA06A", "#0047AB"),
                fashionistaScore = 92.7f,
                colorHarmony = 98.0f,
                silhouette = 85.0f,
                contrastDepth = 95.0f,
                intentStatus = IntentUiStatus.SPECIFIED,
                observedColorfulness = 0.88f,
                observedColorContrast = 0.82f,
                executionTier = ExecutionTier.AI_CLOUD,
                latencyMs = 1290L
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
private fun StyleCreationStoryScreenPreviewLong() {
    MaterialTheme {
        StyleCreationStoryScreen(
            model = StyleCreationUiModel(
                occasion = "Daily Outfit",
                userIntent = "fun colorful outfit",
                appearanceTemperature = "Neutral",
                appearanceDepth = "Light",
                appearanceContrast = "Balanced",
                temperatureC = 23.4f,
                uvIndex = 5.75f,
                circadianContext = "Defense & Protection",
                eligibleWardrobeCount = 53,
                anchorName = "Electric Coral Cropped Hoodie",
                anchorId = "w_3",
                anchorReason = "[INTENT ANCHOR] High-chroma intent override",
                clothing = listOf(
                    StyleItemUiModel(id = "w_3", name = "Electric Coral Cropped Hoodie", role = "TOP", colorHex = "#FF5F1F", temperature = "WARM", material = "100% Organic Cotton"),
                    StyleItemUiModel(id = "w_35", name = "Warm Ivory Pleated Trousers", role = "BOTTOM", colorHex = "#EDD5B1", temperature = "NEUTRAL", material = "Cotton Blend"),
                    StyleItemUiModel(id = "w_48", name = "Camel Leather Boots", role = "SHOES", colorHex = "#BDA06A", temperature = "WARM", material = "Full Grain Leather")
                ),
                cosmetics = listOf(
                    StyleItemUiModel(id = "c_123", name = "Golden Hour Shimmer", role = "EYE", colorHex = "#FFD700", temperature = "NEUTRAL"),
                    StyleItemUiModel(id = "c_78", name = "Natural Peach Blush", role = "CHEEK", colorHex = "#FFA07A", temperature = "WARM"),
                    StyleItemUiModel(id = "c_114", name = "Warm Terracotta Lipstick", role = "LIP", colorHex = "#C75B39", temperature = "WARM"),
                    StyleItemUiModel(id = "c_133", name = "Cobalt Core Polish", role = "NAIL", colorHex = "#0047AB", temperature = "COOL")
                ),
                aiRationale = "Selected an energetic Electric Coral Cropped Hoodie anchored with warm neutral pleated trousers and camel boots for an elevated, vibrant daily look.",
                validationItems = listOf(
                    ValidationUiModel("Mandatory anchor included", true),
                    ValidationUiModel("Top / Bottom / Shoes composition", true),
                    ValidationUiModel("Eye / Cheek / Lip / Nail roles", true),
                    ValidationUiModel("All selected IDs grounded", true),
                    ValidationUiModel("Forbidden PREP items excluded", true)
                ),
                paletteHex = listOf("#FF5F1F", "#EDD5B1", "#BDA06A", "#0047AB"),
                fashionistaScore = 92.7f,
                colorHarmony = 98.0f,
                silhouette = 85.0f,
                contrastDepth = 95.0f,
                intentStatus = IntentUiStatus.SPECIFIED,
                observedColorfulness = 0.88f,
                observedColorContrast = 0.82f,
                executionTier = ExecutionTier.AI_CLOUD,
                latencyMs = 1290L
            )
        )
    }
}

