Yes. Based on this audit trail, I would **not** make this a traditional “recommendation result” screen. The user should see a visual story of **how KoColor arrived at the outfit**.

The screen should answer:

> **What did I ask? What did KoColor know? What did it choose? Why? How was it validated? How good is the result?**

For the current run, the UI should make these facts visible:

```text
User Intent
None specified

Context
Daily • Neutral • Light • Balanced
23.43°C • UV 5.75
Defense & Protection

Deterministic Selection
53 wardrobe items survived filtering
w_41 selected as automatic anchor

Candidate reasoning
w_41 + w_30 + w_48

AI synthesis
Gemini assembled the final outfit

Validation
4 cosmetic roles satisfied
No PREP/forbidden categories selected
Anchor satisfied

FASHIONISTA
85.4 / 100

Observed Style Character
Colorfulness 0.53
Color Contrast 0.50
```

### Recommended Compose experience

I would use a **vertical "Style Journey" timeline** rather than several unrelated cards.

```text
┌──────────────────────────────────────┐
│ YOUR STYLE                          │
│                                      │
│ How KoColor created this look        │
│                                      │
│ ┌──────────────────────────────────┐ │
│ │ CONTEXT                          │ │
│ │ Daily                           │ │
│ │ Neutral • Light • Balanced       │ │
│ │ 23.4°C   UV 5.75                 │ │
│ └──────────────────────────────────┘ │
│                 │                    │
│                 ●                    │
│                 │                    │
│ ┌──────────────────────────────────┐ │
│ │ 01  UNDERSTOOD YOUR CONTEXT      │ │
│ │ 53 wardrobe items eligible       │ │
│ │ No specific style preference     │ │
│ └──────────────────────────────────┘ │
│                 │                    │
│                 ●                    │
│                 │                    │
│ ┌──────────────────────────────────┐ │
│ │ 02  FOUND ANCHOR                 │ │
│ │ Universal Khaki Button-Down      │ │
│ │ Automatic context anchor         │ │
│ └──────────────────────────────────┘ │
│                 │                    │
│                 ●                    │
│                 │                    │
│ ┌──────────────────────────────────┐ │
│ │ 03  BUILT THE OUTFIT             │ │
│ │ TOP    Khaki Button-Down         │ │
│ │ BOTTOM Khaki Cargo               │ │
│ │ SHOES  Camel Ankle Boots         │ │
│ └──────────────────────────────────┘ │
│                 │                    │
│                 ●                    │
│                 │                    │
│ ┌──────────────────────────────────┐ │
│ │ 04  AI STYLE SYNTHESIS           │ │
│ │ Gemini combined the candidates   │ │
│ │ with the required constraints    │ │
│ └──────────────────────────────────┘ │
│                 │                    │
│                 ●                    │
│                 │                    │
│ ┌──────────────────────────────────┐ │
│ │ 05  VERIFIED                     │ │
│ │ ✓ Anchor                         │ │
│ │ ✓ Top / Bottom / Shoes           │ │
│ │ ✓ Eye / Cheek / Lip / Nail       │ │
│ │ ✓ Grounded selections            │ │
│ └──────────────────────────────────┘ │
│                                      │
│ ┌──────────────────────────────────┐ │
│ │ FASHIONISTA                      │ │
│ │             85.4                 │ │
│ │        / 100                     │ │
│ │                                  │ │
│ │ Color Harmony       98           │ │
│ │ Silhouette          75           │ │
│ │ Contrast & Depth    80           │ │
│ └──────────────────────────────────┘ │
└──────────────────────────────────────┘
```

Here is a Compose implementation I would use as the foundation.

```kotlin
@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StyleCreationUiModel(
    val occasion: String,
    val userIntent: String?,
    val appearanceTemperature: String,
    val appearanceDepth: String,
    val appearanceContrast: String,
    val temperatureC: Float?,
    val uvIndex: Float?,
    val circadianContext: String,
    val eligibleWardrobeCount: Int,
    val anchorName: String,
    val anchorId: String,
    val anchorReason: String,
    val clothing: List<StyleItemUiModel>,
    val cosmetics: List<StyleItemUiModel>,
    val aiRationale: String,
    val validationItems: List<ValidationUiModel>,
    val paletteHex: List<String>,
    val fashionistaScore: Float,
    val colorHarmony: Float,
    val silhouette: Float,
    val contrastDepth: Float,
    val intentStatus: IntentUiStatus,
    val observedColorfulness: Float?,
    val observedColorContrast: Float?
)

data class StyleItemUiModel(
    val id: String,
    val name: String,
    val role: String,
    val colorHex: String,
    val temperature: String?,
    val material: String?
)

data class ValidationUiModel(
    val label: String,
    val passed: Boolean
)

enum class IntentUiStatus {
    NOT_SPECIFIED,
    SPECIFIED
}

@Composable
fun StyleCreationStoryScreen(
    model: StyleCreationUiModel,
    modifier: Modifier = Modifier
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

            item {
                OutfitAssemblyCard(
                    clothing = model.clothing
                )
            }

            item {
                CosmeticSelectionCard(
                    cosmetics = model.cosmetics
                )
            }

            item {
                TimelineStep(
                    number = "04",
                    icon = Icons.Outlined.AutoAwesome,
                    title = "AI style synthesis",
                    subtitle = "Gemini assembled the final recommendation from the grounded candidate set.",
                    body = model.aiRationale
                )
            }

            item {
                ValidationCard(
                    validations = model.validationItems
                )
            }

            item {
                PaletteCard(
                    palette = model.paletteHex
                )
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
```

### Context card

```kotlin
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
                    androidx.compose.foundation.rememberScrollState()
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
```

### Timeline step

```kotlin
@Composable
private fun TimelineStep(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
```

### Outfit assembly

```kotlin
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
```

### Cosmetics

```kotlin
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
```

### Validation

This is especially important because it tells the user that the AI didn't simply do whatever it wanted.

```kotlin
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
```

Feed it:

```kotlin
listOf(
    ValidationUiModel("Mandatory anchor included", true),
    ValidationUiModel("Top / Bottom / Shoes composition", true),
    ValidationUiModel("Eye / Cheek / Lip / Nail roles", true),
    ValidationUiModel("All selected IDs grounded", true),
    ValidationUiModel("Forbidden PREP items excluded", true),
    ValidationUiModel("Rationale references selected items only", true)
)
```

### FASHIONISTA card

This should feel different visually because it is **measurement**, not AI reasoning.

```kotlin
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
```

### Intent / Style Character

For **this exact run**, don't display:

```text
Intent Fulfillment: 93
```

because the log says:

```text
Status: NOT_SPECIFIED
```

Instead, show:

```text
STYLE CHARACTER

No specific style preference was provided.

Colorfulness      53
Color Contrast    50
```

```kotlin
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
```

### Palette

```kotlin
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
```

### Helpers

```kotlin
@Composable
private fun SectionHeader(
    title: String,
    headline: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
private fun parseHex(hex: String): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(MaterialTheme.colorScheme.surfaceVariant)
}
```

## One architectural change I'd make to your existing UI model

Don't make the UI reconstruct the audit trail.

Have the engine produce a structured result:

```kotlin
data class StyleCreationResult(
    val context: StyleContextSummary,
    val anchor: AnchorDecision,
    val candidateSummary: CandidateSummary,
    val blueprint: StyleBlueprint,
    val validation: RecommendationValidationResult,
    val fashionista: FashionistaResult,
    val intent: IntentEvaluationResult
)
```

Then Compose simply renders it.

That is much better than having `StyleResultViewModel` infer things from logs.

### And I would make one important UX distinction

The raw log contains this:

```text
Feature 646 is not available
Firebase AI Logic
```

I would **not show that exception to the user**.

Instead, optionally expose a small technical disclosure:

```text
AI processing
✓ On-device AI checked
→ Cloud AI used for synthesis
```

with the raw error available only in diagnostics/developer mode.

The user cares about:

> **How my outfit was created**

not:

> `GenAiException: FEATURE_NOT_FOUND: Feature 646`.

This UI gives KoColor something much more interesting than a generic "AI recommendation" screen: it shows the user a **visual, inspectable fashion-generation process**, while keeping the deterministic selection, Gemini synthesis, validation, and FASHIONISTA measurement clearly separated.
