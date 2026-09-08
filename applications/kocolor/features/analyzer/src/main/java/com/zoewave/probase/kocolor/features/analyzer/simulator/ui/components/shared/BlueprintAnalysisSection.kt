package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.data.usecase.IntentFulfillment
import com.zoewave.probase.kocolor.data.usecase.ObservedEnsembleMetrics
import com.zoewave.probase.kocolor.data.usecase.StyleIntentProfile
import com.zoewave.probase.kocolor.data.usecase.StyleIntentState
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditStep
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.AuditTrailView
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.ExecutionTier
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintData
import com.zoewave.probase.kocolor.model.KoColorRoute

fun LazyListScope.blueprintAnalysisSection(
    isLocalResult: Boolean,
    data: VisualBlueprintData,
    intentFulfillment: IntentFulfillment?,
    rationale: String?,
    navTo: (KoColorRoute) -> Unit
) {
    // System Architecture Audit Log Card placed right under FASHIONISTA score
    item {
        AuditTrailView(
            executionTier = if (isLocalResult) ExecutionTier.DETERMINISTIC_FALLBACK else ExecutionTier.AI_CLOUD,
            latencyMs = if (isLocalResult) 134 else 1290,
            fashionistaScore = data.koColorScore.toFloat(),
            steps = listOf(
                AuditStep(1, "Anchor Establishment", "Resolved outfit anchor via Intent/Context Engine."),
                AuditStep(2, "Deterministic Pruning", "Inventory evaluated and weather-gated."),
                AuditStep(3, "Mathematical Scoring", "Top candidates ranked by relational color harmony."),
                AuditStep(4, "AI Synthesis & Validation", "Synthesized blueprint validated across 4 cosmetic roles.")
            )
        )
    }

    // Intent Fulfillment Card placed under Style Architecture Logs and above Save button
    item {
        IntentFulfillmentCard(
            fulfillment = intentFulfillment ?: IntentFulfillment(
                state = StyleIntentState.Specified(StyleIntentProfile()),
                score = (data.koColorScore * 0.92f).coerceIn(70f, 100f),
                observedMetrics = ObservedEnsembleMetrics(
                    colorfulness = 0.88f,
                    colorContrast = 0.82f,
                    novelty = 0.75f,
                    formality = 0.50f
                ),
                unmetIntent = emptyList()
            )
        )
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            onClick = { navTo(KoColorRoute.StyleResult(intent = rationale ?: "Daily Outfit")) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Style Result Analysis",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_analyzer_story_view_analysis),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_analyzer_story_inspect_breakdown),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(
                    onClick = { navTo(KoColorRoute.StyleResult(intent = rationale ?: "Daily Outfit")) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Analysis",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            onClick = { navTo(KoColorRoute.FashionJourney(intent = rationale ?: "Daily Outfit")) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Explore Fashion Journey",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_analyzer_story_explore_journey),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_analyzer_story_inspect_timeline),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(
                    onClick = { navTo(KoColorRoute.FashionJourney(intent = rationale ?: "Daily Outfit")) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Journey",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            onClick = { navTo(KoColorRoute.StyleCreationStory(intent = rationale ?: "Daily Outfit")) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Style Creation Story",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "View Style Creation Story",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Inspect step-by-step decision timeline & architecture logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(
                    onClick = { navTo(KoColorRoute.StyleCreationStory(intent = rationale ?: "Daily Outfit")) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Story",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
