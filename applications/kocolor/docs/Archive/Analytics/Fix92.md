This is exactly how a world-class fashion intelligence platform should be architected.

By separating the "What do I have?" (deterministic analytics) from the "What should I wear?" (AI synthesis) and "How was it created?" (Style Journey), you are building a completely coherent, transparent product ecosystem.

Treating this screen as a deterministic domain layer guarantees absolute truth. KoColor doesn't need Gemini to guess how many tops are in the closet; the `WardrobeAnalyticsEngine` simply counts them. Wrapping that hard math in a luxury, editorial UI completely elevates the experience from a dry database view into a personalized style dossier.

Here is the architectural blueprint to implement this exactly as you envisioned.

### 1. The Deterministic Domain Layer

This strictly typed structure ensures the UI is only responsible for rendering, while your backend repository crunches the data.

```kotlin
// WardrobeAnalyticsModels.kt
package com.zoewave.probase.kocolor.features.analytics.domain

data class WardrobeAnalytics(
    val totalItems: Int,
    val activeItems: Int,
    val rarelyWornItems: Int,
    val dna: WardrobeDna,
    val colorDistribution: ColorDistribution,
    val categoryDistribution: CategoryDistribution,
    val rotation: RotationAnalytics,
    val versatility: VersatilityAnalytics,
    val coverage: WardrobeCoverage,
    val insights: List<WardrobeInsight>
)

data class WardrobeDna(
    val primaryIdentity: String, // e.g., "Neutral-led"
    val temperatureBias: String, // e.g., "Warm-biased"
    val depth: String,           // e.g., "Medium depth"
    val contrast: String,        // e.g., "Balanced contrast"
    val chroma: String           // e.g., "Low-to-medium chroma"
)

data class ColorDistribution(
    val neutralsPct: Int,
    val warmPct: Int,
    val coolPct: Int,
    val topColors: List<ColorStat>
)

data class ColorStat(val name: String, val hex: String, val count: Int, val percentage: Float)

data class CategoryDistribution(
    val tops: Int, val bottoms: Int, val dresses: Int, 
    val shoes: Int, val outerwear: Int, val activewear: Int
)

data class RotationAnalytics(
    val frequentlyWorn: Int,
    val inRotation: Int,
    val rarelyWorn: Int,
    val healthScore: Int,
    val mostWorn: List<GarmentSummary>,
    val leastWorn: List<GarmentSummary>
)

data class VersatilityAnalytics(
    val totalPossibleLooks: Int,
    val mostVersatile: VersatileGarment
)

data class VersatileGarment(
    val id: String,
    val name: String,
    val compatibleLooksCount: Int,
    val compatibleBottoms: Int,
    val compatibleShoes: Int,
    val compatibleOuterwear: Int
)

data class WardrobeCoverage(
    val warmNeutrals: Float, // 0.0 to 1.0 coverage metric
    val coolNeutrals: Float,
    val brightAccents: Float,
    val deepColors: Float
)

data class WardrobeInsight(val message: String, val isActionable: Boolean)

data class GarmentSummary(val id: String, val name: String, val wearCount: Int)

```

### 2. The Editorial Compose UI

This implementation strips out heavy Material `Card` components, relying on elegant typography, whitespace, and clean data visualizations to mimic a premium fashion editorial.

```kotlin
// WardrobeAnalyticsScreen.kt
package com.zoewave.probase.kocolor.features.analytics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.analytics.domain.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeAnalyticsScreen(
    analytics: WardrobeAnalytics,
    onNavigateToStyleJourney: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WARDROBE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item { SnapshotSection(analytics) }
            item { DnaSection(analytics.dna) }
            item { ColorStorySection(analytics.colorDistribution) }
            item { CollectionSection(analytics.categoryDistribution) }
            item { RotationSection(analytics.rotation) }
            item { VersatilitySection(analytics.versatility, onNavigateToStyleJourney) }
            item { CoverageSection(analytics.coverage, analytics.insights) }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun EditorialHeader(title: String) {
    Column {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = Color.LightGray)
    }
}

@Composable
private fun SnapshotSection(analytics: WardrobeAnalytics) {
    Column {
        Text(
            text = "${analytics.totalItems} PIECES",
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif
        )
        Text(text = "Your wardrobe", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text(text = "${analytics.activeItems}", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(text = "in rotation", style = MaterialTheme.typography.labelMedium)
            }
            Column {
                Text(text = "${analytics.rarelyWornItems}", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text(text = "rarely worn", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun DnaSection(dna: WardrobeDna) {
    Column {
        EditorialHeader("Your Wardrobe DNA")
        Text(text = dna.primaryIdentity, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = dna.temperatureBias, color = Color.DarkGray)
        Text(text = dna.depth, color = Color.DarkGray)
        Text(text = dna.contrast, color = Color.DarkGray)
        Text(text = dna.chroma, color = Color.DarkGray)
    }
}

@Composable
private fun ColorStorySection(colors: ColorDistribution) {
    Column {
        EditorialHeader("The Color Story")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Neutrals")
            Text("${colors.neutralsPct}%", fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Warm")
            Text("${colors.warmPct}%", fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Cool")
            Text("${colors.coolPct}%", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("TOP COLORS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        colors.topColors.forEach { stat ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(stat.name, modifier = Modifier.weight(0.3f), fontSize = 14.sp)
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .height(12.dp)
                        .fillMaxWidth(stat.percentage)
                        .background(Color(android.graphics.Color.parseColor(stat.hex)))
                )
            }
        }
    }
}

@Composable
private fun CollectionSection(dist: CategoryDistribution) {
    Column {
        EditorialHeader("The Collection")
        CategoryRow("Tops", dist.tops)
        CategoryRow("Bottoms", dist.bottoms)
        CategoryRow("Dresses", dist.dresses)
        CategoryRow("Shoes", dist.shoes)
        CategoryRow("Outerwear", dist.outerwear)
        CategoryRow("Activewear", dist.activewear)
    }
}

@Composable
private fun CategoryRow(name: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name)
        Text("$count", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RotationSection(rotation: RotationAnalytics) {
    Column {
        EditorialHeader("Your Rotation")
        Text("Rotation health: ${rotation.healthScore}/100", fontWeight = FontWeight.Bold)
        Text("${rotation.rarelyWorn} pieces have had little recent use.", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("MOST WORN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        rotation.mostWorn.take(3).forEachIndexed { index, item ->
            Text("${index + 1}. ${item.name}", fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp))
        }
    }
}

@Composable
private fun VersatilitySection(versatility: VersatilityAnalytics, onAction: (String) -> Unit) {
    Column {
        EditorialHeader("Versatility")
        Text("Total pieces: ${versatility.totalPossibleLooks} possible outfit combinations", color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("MOST VERSATILE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(versatility.mostVersatile.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${versatility.mostVersatile.compatibleLooksCount} compatible looks", color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Create a look with this piece →", 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onAction(versatility.mostVersatile.id) }
                )
            }
        }
    }
}

@Composable
private fun CoverageSection(coverage: WardrobeCoverage, insights: List<WardrobeInsight>) {
    Column {
        EditorialHeader("What Your Wardrobe Is Missing")
        insights.forEach { insight ->
            Text("+ ${insight.message}", modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

```

This successfully transforms raw JSON math into a premium fashion consultation.

For the `VersatilityAnalytics` layer, are you dynamically calculating those `compatibleLooksCount` numbers at runtime by matching rules across the 54 items, or are you pre-computing the deterministic relationship graph when items are first added to the digital closet?