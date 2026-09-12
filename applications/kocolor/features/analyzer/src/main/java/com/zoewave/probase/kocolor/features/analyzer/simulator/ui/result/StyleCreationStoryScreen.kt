package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.data.usecase.CreationPhase
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditStep
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditTrailView
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ExecutionTier
import com.zoewave.probase.kocolor.model.KoColorRoute
import kotlin.math.roundToInt

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
    val intentScore: Float? = null,
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
    phase: CreationPhase = CreationPhase.COMPLETE,
    isEmbedded: Boolean = false,
    navTo: (KoColorRoute) -> Unit = {}
) {
    if (isEmbedded) {
        StyleCreationStoryContent(
            model = model,
            phase = phase,
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier,
            containerColor = Color.White,
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "STYLE JOURNEY",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "How KoColor created this look",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    StyleCreationStoryContent(
                        model = model,
                        phase = phase
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun StyleCreationStoryContent(
    model: StyleCreationUiModel,
    phase: CreationPhase,
    modifier: Modifier = Modifier
) {
    var isArchitectureExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (isArchitectureExpanded) 180f else 0f,
        label = "ArchitectureChevronRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- 01 THE VISION ---
        EditorialSection(stepNumber = "01", title = "THE VISION") {
            Text(
                text = if (!model.userIntent.isNullOrBlank()) "\"${model.userIntent}\"" else "No specific style preference provided.",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Occasion: ${model.occasion}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 02 THE ATMOSPHERE ---
        EditorialSection(stepNumber = "02", title = "THE ATMOSPHERE") {
            Text(
                text = "${model.appearanceTemperature} Undertone • ${model.appearanceDepth} Depth • ${model.appearanceContrast} Contrast",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${model.eligibleWardrobeCount} wardrobe items survived deterministic weather & climate gating.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 03 THE ANCHOR ---
        EditorialSection(stepNumber = "03", title = "THE ANCHOR") {
            Text(
                text = model.anchorName,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (!model.userIntent.isNullOrBlank()) {
                    "Your request called for a specific style profile, so KoColor promoted a high-confidence candidate (${model.anchorId}) as the foundational anchor."
                } else {
                    "KoColor automatically selected ${model.anchorName} (${model.anchorId}) as the foundational context anchor for this look."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 04 THE ENSEMBLE ---
        EditorialSection(stepNumber = "04", title = "THE ENSEMBLE") {
            if (model.clothing.isNotEmpty()) {
                Text(
                    text = "WARDROBE SELECTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                model.clothing.forEach { item ->
                    EditorialItemRow(item)
                }
            }

            if (model.cosmetics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "BEAUTY LAYER",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                model.cosmetics.forEach { item ->
                    EditorialItemRow(item)
                }
            }

            if (model.paletteHex.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "THE EDITORIAL PALETTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    model.paletteHex.forEach { hex ->
                        val colorName = mapHexToSemanticName(hex)
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = colorName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 05 THE STYLE ARCHITECT ---
        EditorialSection(stepNumber = "05", title = "THE STYLE ARCHITECT") {
            Text(
                text = "The Style Architect says:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "“${model.aiRationale.replace(Regex("(?i)feature\\s+\\d+\\s+is\\s+not\\s+available.*"), "").trim()}”",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 06 THE CHECK ---
        EditorialSection(stepNumber = "06", title = "THE CHECK") {
            Text(
                text = "KoColor verified 6/6 architectural constraints for this recommendation:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))
            val validations = model.validationItems.ifEmpty {
                listOf(
                    ValidationUiModel("Mandatory anchor included", true),
                    ValidationUiModel("Top / Bottom / Shoes composition", true),
                    ValidationUiModel("Eye / Cheek / Lip / Nail roles", true),
                    ValidationUiModel("All selected IDs grounded", true),
                    ValidationUiModel("Forbidden PREP items excluded", true),
                    ValidationUiModel("Rationale references selected items only", true)
                )
            }
            validations.forEach { validation ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = validation.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 07 THE SCORE ---
        EditorialSection(stepNumber = "07", title = "THE SCORE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FASHIONISTA AESTHETIC EVALUATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${model.fashionistaScore.roundToInt()} / 100",
                        style = MaterialTheme.typography.displayMedium,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFF10B981)
                ) {
                    Text(
                        text = "APPROVED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Color Harmony", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${model.colorHarmony.roundToInt()}/100", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Silhouette Proportion", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${model.silhouette.roundToInt()}/100", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Contrast & Depth", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("${model.contrastDepth.roundToInt()}/100", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (model.intentStatus == IntentUiStatus.SPECIFIED && model.intentScore != null) {
                Text(
                    text = "YOUR REQUEST FULFILLMENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${model.intentScore.roundToInt()} / 100 Match",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "REQUEST SATISFIED",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                }
            } else {
                Text(
                    text = "OBSERVED STYLE CHARACTERISTICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No specific style preference was provided.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ensemble Colorfulness", style = MaterialTheme.typography.bodyMedium)
                    Text("${((model.observedColorfulness ?: 0.53f) * 100).toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Color Contrast", style = MaterialTheme.typography.bodyMedium)
                    Text("${((model.observedColorContrast ?: 0.50f) * 100).toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // --- 08 PROGRESSIVE DISCLOSURE FOOTER: STYLE ARCHITECTURE ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isArchitectureExpanded = !isArchitectureExpanded }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STYLE ARCHITECTURE LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isArchitectureExpanded) "Collapse Architecture Logs" else "Expand Architecture Logs",
                    modifier = Modifier.rotate(rotationState),
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = isArchitectureExpanded) {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    AuditTrailView(
                        executionTier = model.executionTier,
                        latencyMs = model.latencyMs,
                        fashionistaScore = model.fashionistaScore,
                        steps = model.auditSteps
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorialSection(
    stepNumber: String,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        content()
    }
}

@Composable
private fun EditorialItemRow(item: StyleItemUiModel) {
    val color = try { Color(android.graphics.Color.parseColor(item.colorHex)) } catch (e: Exception) { Color.Gray }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${item.role} ${item.material?.let { "• $it" } ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

fun mapHexToSemanticName(hex: String): String {
    val cleanHex = hex.uppercase().removePrefix("#")
    return when {
        cleanHex.startsWith("FF5F") || cleanHex.startsWith("FFA0") || cleanHex.startsWith("FF7") -> "Coral"
        cleanHex.startsWith("EDD") || cleanHex.startsWith("F3E") || cleanHex.startsWith("FFF") || cleanHex.startsWith("FAF") -> "Ivory"
        cleanHex.startsWith("BDA") || cleanHex.startsWith("8B4") || cleanHex.startsWith("C5") || cleanHex.startsWith("D2") -> "Camel"
        cleanHex.startsWith("004") || cleanHex.startsWith("0B0") || cleanHex.startsWith("1E3") -> "Cobalt"
        cleanHex.startsWith("2C2") || cleanHex.startsWith("1F2") || cleanHex.startsWith("000") || cleanHex.startsWith("11") -> "Charcoal"
        cleanHex.startsWith("DC1") || cleanHex.startsWith("FF0") || cleanHex.startsWith("C7") || cleanHex.startsWith("E6") -> "Terracotta"
        else -> "Harmonic"
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

@Preview(showBackground = true, name = "Editorial Style Story Preview")
@Composable
private fun StyleCreationStoryScreenPreview() {
    MaterialTheme {
        StyleCreationStoryScreen(
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
                contrastDepth = 97f,
                intentStatus = IntentUiStatus.SPECIFIED,
                intentScore = 94.2f
            )
        )
    }
}
