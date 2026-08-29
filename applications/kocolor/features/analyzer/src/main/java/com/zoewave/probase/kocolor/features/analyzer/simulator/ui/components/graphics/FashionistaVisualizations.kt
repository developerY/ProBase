package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaFeatureVector
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import kotlin.math.cos
import kotlin.math.sin

/**
 * Data class representing the raw mathematical output of the deterministic scorer.
 */
data class FashionistaMathBreakdown(
    val qBase: Double = 0.82,             // Range: 0.0 - 1.0
    val qInteraction: Double = 0.88,      // Range: 0.0 - 1.0
    val effectiveLambda: Double = 0.20,   // e.g., 0.20
    val unresolvedPenalty: Double = 0.0, // Range: 0.0 - 0.5
    val qFinal: Double = 0.832,            // Bounded: 0.0 - 1.0
    val finalScore: Int = 87            // Calibrated: 0 - 100
)

/**
 * Component 1: Pearlescent Hero Dial (Based on design reference image_c2a0ca.png)
 * Features segmented feature sweeps, coverage track with glowing node caps, and organic shimmer.
 */
@Composable
fun FashionistaHeroDial(
    score: Double,
    coverage: Double,
    breakdown: FashionistaFeatureVector? = null,
    modifier: Modifier = Modifier
) {
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(score, coverage) {
        animationTriggered = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) (score.coerceIn(0.0, 100.0) / 100.0).toFloat() else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "scoreProgress"
    )

    val animatedCoverage by animateFloatAsState(
        targetValue = if (animationTriggered) coverage.coerceIn(0.0, 1.0).toFloat() else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "coverageProgress"
    )

    val animatedScoreInt by animateIntAsState(
        targetValue = if (animationTriggered) score.coerceIn(0.0, 100.0).toInt() else 0,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "scoreInt"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    val shimmerShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerShift"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val outerRadius = size.minDimension / 2

                // 1. Outer Pearlescent Disk Base
                val pearlescentBrush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFF3E7D3), // Champagne
                        Color(0xFFE8C8D5), // Soft Rose
                        Color(0xFFDCD5EF), // Lavender
                        Color(0xFFC6DAC9), // Soft Sage
                        Color(0xFFE2CBB7), // Warm Taupe
                        Color(0xFFF3E7D3)  // Loop
                    ),
                    center = center
                )

                drawCircle(
                    brush = pearlescentBrush,
                    radius = outerRadius,
                    center = center
                )

                // 2. Beveled White Border Ring
                val borderStrokeWidth = 4.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = outerRadius - borderStrokeWidth / 2,
                    center = center,
                    style = Stroke(width = borderStrokeWidth)
                )

                // 3. Segmented Sweeps Ring (Outer Ring)
                val outerTrackRadius = outerRadius - 16.dp.toPx()
                val outerTrackStrokeWidth = 6.dp.toPx()

                // Draw background track
                drawCircle(
                    color = Color.Black.copy(alpha = 0.06f),
                    radius = outerTrackRadius,
                    center = center,
                    style = Stroke(width = outerTrackStrokeWidth)
                )

                val sweepAngle = animatedProgress * 360f

                // Outer Pearlescent Progress Arc
                val silverBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1),
                        Color.White.copy(alpha = 0.9f),
                        Color(0xFF94A3B8),
                        Color(0xFF7DD3FC),
                        Color(0xFFE2E8F0)
                    ),
                    start = Offset(center.x - outerTrackRadius + (shimmerShift % 400f), center.y - outerTrackRadius + (shimmerShift % 400f)),
                    end = Offset(center.x + outerTrackRadius + (shimmerShift % 400f), center.y + outerTrackRadius + (shimmerShift % 400f))
                )

                drawArc(
                    brush = silverBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = outerTrackStrokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - outerTrackRadius, center.y - outerTrackRadius),
                    size = Size(outerTrackRadius * 2, outerTrackRadius * 2)
                )

                // 4. Inner Coverage Track (Thinner Track with Glowing Node Caps)
                val innerTrackRadius = outerTrackRadius - 14.dp.toPx()
                val innerTrackStrokeWidth = 3.dp.toPx()

                // Inactive Coverage Track
                drawCircle(
                    color = Color.Black.copy(alpha = 0.05f),
                    radius = innerTrackRadius,
                    center = center,
                    style = Stroke(width = innerTrackStrokeWidth)
                )

                val coverageSweepAngle = animatedCoverage * 360f

                if (coverageSweepAngle > 0f) {
                    val iceBlueGlow = Color(0xFF38BDF8)

                    // Inner Coverage Arc
                    drawArc(
                        color = iceBlueGlow.copy(alpha = 0.75f),
                        startAngle = -90f,
                        sweepAngle = coverageSweepAngle,
                        useCenter = false,
                        style = Stroke(width = innerTrackStrokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - innerTrackRadius, center.y - innerTrackRadius),
                        size = Size(innerTrackRadius * 2, innerTrackRadius * 2)
                    )

                    // Glowing Node Caps at start and end of coverage arc
                    val endAngleRad = Math.toRadians((-90f + coverageSweepAngle).toDouble())

                    val endCapX = center.x + (innerTrackRadius * cos(endAngleRad)).toFloat()
                    val endCapY = center.y + (innerTrackRadius * sin(endAngleRad)).toFloat()

                    // End Node Glow
                    drawCircle(
                        color = iceBlueGlow.copy(alpha = 0.35f),
                        radius = 6.dp.toPx(),
                        center = Offset(endCapX, endCapY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(endCapX, endCapY)
                    )
                }

                // 5. Inner White Core Disc
                val innerCoreRadius = innerTrackRadius - 10.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = innerCoreRadius,
                    center = center
                )
            }

            // Score Display Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$animatedScoreInt",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 68.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color.Black
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.applications_kocolor_features_analyzer_fashionista_title),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )
    }
}

/**
 * Component 2: 6-Axis Aesthetic Radar (Spider Chart)
 * Renders the hexagonal feature shape of the outfit. Dashed lines indicate partial availability.
 */
@Composable
fun FashionistaRadarChart(
    breakdown: FashionistaFeatureVector,
    modifier: Modifier = Modifier
) {
    val labels = listOf("Comp", "Color", "Shape", "Texture", "Hierarchy", "Integration")
    val features = listOf(
        breakdown.composition,
        breakdown.colorHarmony,
        breakdown.silhouette,
        breakdown.textureHarmony,
        breakdown.visualHierarchy,
        breakdown.presentationIntegration
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.minDimension / 2 - 12.dp.toPx()
                val angleStep = (2 * Math.PI / 6).toFloat()

                // 1. Draw Hexagonal Grid Background (25%, 50%, 75%, 100%)
                for (step in 1..4) {
                    val r = maxRadius * (step / 4f)
                    val gridPath = Path()
                    for (i in 0 until 6) {
                        val angle = -Math.PI / 2 + i * angleStep
                        val x = center.x + r * cos(angle).toFloat()
                        val y = center.y + r * sin(angle).toFloat()
                        if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                    }
                    gridPath.close()
                    drawPath(
                        path = gridPath,
                        color = Color.Black.copy(alpha = 0.06f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Draw 6 Spoke Axis Lines
                for (i in 0 until 6) {
                    val angle = -Math.PI / 2 + i * angleStep
                    val endX = center.x + maxRadius * cos(angle).toFloat()
                    val endY = center.y + maxRadius * sin(angle).toFloat()
                    drawLine(
                        color = Color.Black.copy(alpha = 0.1f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Draw Data Polygon Vertices
                val polyPath = Path()
                val points = mutableListOf<Offset>()

                for (i in 0 until 6) {
                    val feature = features[i]
                    val r = (maxRadius * feature.value.coerceIn(0.0, 1.0)).toFloat()
                    val angle = -Math.PI / 2 + i * angleStep
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()
                    val pt = Offset(x, y)
                    points.add(pt)
                    if (i == 0) polyPath.moveTo(x, y) else polyPath.lineTo(x, y)
                }
                polyPath.close()

                // Filled Polygon Area
                drawPath(
                    path = polyPath,
                    color = Color(0xFF2F6364).copy(alpha = 0.25f)
                )

                // Outline Edges (Dashed line if availability < 1.0)
                for (i in 0 until 6) {
                    val nextIdx = (i + 1) % 6
                    val avail = (features[i].availability * features[nextIdx].availability).toFloat()
                    val startPt = points[i]
                    val endPt = points[nextIdx]

                    if (avail < 0.99f) {
                        // Dashed path for low availability/missing evidence
                        val dashedPath = Path().apply {
                            moveTo(startPt.x, startPt.y)
                            lineTo(endPt.x, endPt.y)
                        }
                        drawPath(
                            path = dashedPath,
                            color = Color(0xFF2F6364).copy(alpha = 0.5f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    } else {
                        drawLine(
                            color = Color(0xFF2F6364),
                            start = startPt,
                            end = endPt,
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }

                // Vertex Dots
                points.forEachIndexed { i, pt ->
                    val avail = features[i].availability.toFloat()
                    drawCircle(
                        color = Color(0xFF2F6364).copy(alpha = avail),
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // Radar Axis Labels Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            labels.forEachIndexed { i, label ->
                val valPct = (features[i].value * 100).toInt()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$valPct%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Component 3: MATH Tab - Detailed Waterfall Math Decomposition
 * Visually audits the deterministic scoring equation:
 * Q = (1 - λ)Q_base + λ Q_interaction - P_unresolved
 */
@Composable
fun FashionistaMathDecomposition(
    breakdown: FashionistaMathBreakdown,
    modifier: Modifier = Modifier
) {
    var animTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(breakdown) {
        animTriggered = true
    }

    val animatedAnimProgress by animateFloatAsState(
        targetValue = if (animTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "waterfallAnim"
    )

    val baseWidthRatio = ((1.0 - breakdown.effectiveLambda) * breakdown.qBase).coerceIn(0.0, 1.0).toFloat()
    val interWidthRatio = (breakdown.effectiveLambda * breakdown.qInteraction).coerceIn(0.0, 1.0).toFloat()
    val penaltyRatio = breakdown.unresolvedPenalty.coerceIn(0.0, 0.5).toFloat()
    val netWidthRatio = (baseWidthRatio + interWidthRatio - penaltyRatio).coerceIn(0.0f, 1.0f)

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_analyzer_fashionista_equation_title),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Q = (1 - λ)Q_base + λ Q_interaction - P_unresolved",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        // Canvas Waterfall Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.05f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp)) {
                val barWidth = size.width * animatedAnimProgress
                val h = size.height

                val basePixelWidth = (barWidth * baseWidthRatio).coerceAtMost(barWidth)
                val interPixelWidth = (barWidth * interWidthRatio).coerceAtMost(barWidth - basePixelWidth)

                // 1. Solid Base Score Bar (Dark Slate)
                if (basePixelWidth > 0f) {
                    drawRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(0f, 0f),
                        size = Size(basePixelWidth, h)
                    )
                }

                // 2. Synergy Addition Bar (Premium Muted Green)
                if (interPixelWidth > 0f) {
                    drawRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(basePixelWidth, 0f),
                        size = Size(interPixelWidth, h)
                    )
                }

                // 3. Conflict Penalty (Red Subtraction Bar)
                if (penaltyRatio > 0f) {
                    val penaltyPixelWidth = (barWidth * penaltyRatio).coerceAtMost(basePixelWidth + interPixelWidth)
                    val penaltyStartX = (basePixelWidth + interPixelWidth - penaltyPixelWidth).coerceAtLeast(0f)

                    drawRect(
                        color = Color(0xFFEF4444).copy(alpha = 0.85f),
                        topLeft = Offset(penaltyStartX, 0f),
                        size = Size(penaltyPixelWidth, h)
                    )
                }

                // 4. Sharp Q-Marker Tick at Net qFinal Position
                val qMarkerX = (barWidth * netWidthRatio).toFloat()
                drawLine(
                    color = Color.Black,
                    start = Offset(qMarkerX, -2.dp.toPx()),
                    end = Offset(qMarkerX, h + 2.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Explanatory Labels
        val blendedVal = (1.0 - breakdown.effectiveLambda) * breakdown.qBase + breakdown.effectiveLambda * breakdown.qInteraction

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_fashionista_blended_format, blendedVal),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black
                )
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_fashionista_penalty_format, breakdown.unresolvedPenalty),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (breakdown.unresolvedPenalty > 0.0) Color(0xFFEF4444) else Color.Gray
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_fashionista_mapping_format, breakdown.finalScore),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2F6364)
            )
        }
    }
}

/**
 * Component 4: Decomposition Bar (Waterfall / Math Breakdown)
 * Explains Q_base evidence, Q_interaction synergy, and P_unresolved chaos penalties.
 */
@Composable
fun FashionistaDecompositionBar(
    score: Double,
    breakdown: FashionistaFeatureVector? = null,
    modifier: Modifier = Modifier
) {
    val qBasePct = ((score * 0.85) / 100.0).coerceIn(0.1, 0.85).toFloat()
    val qInterPct = 0.15f
    val pChaosPct = if (score < 60.0) 0.12f else 0.0f

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.applications_kocolor_features_analyzer_fashionista_decomp_title),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )

        Spacer(Modifier.height(12.dp))

        // Horizontal Stacked Waterfall Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.06f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Base Evidence Bar (Solid Dark)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(qBasePct)
                        .background(Color(0xFF1E293B))
                )
                // Interaction Synergy Bar (Green)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(qInterPct)
                        .background(Color(0xFF10B981))
                )
                // Chaos Penalty Bar (Red, if present)
                if (pChaosPct > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(pChaosPct)
                            .background(Color(0xFFEF4444))
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Legend Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1E293B)))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.applications_kocolor_features_analyzer_fashionista_q_base), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.applications_kocolor_features_analyzer_fashionista_q_synergy), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
            }
            if (pChaosPct > 0f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.applications_kocolor_features_analyzer_fashionista_p_chaos), style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FashionistaHeroDialPreview() {
    val sampleVector = FashionistaFeatureVector(
        composition = FeatureValue(0.85, 1.0),
        colorHarmony = FeatureValue(0.92, 1.0),
        silhouette = FeatureValue(0.78, 1.0),
        textureHarmony = FeatureValue(0.80, 0.7),
        visualHierarchy = FeatureValue(0.88, 1.0),
        presentationIntegration = FeatureValue(0.65, 0.5)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        FashionistaHeroDial(score = 87.0, coverage = 0.92, breakdown = sampleVector)
        Spacer(Modifier.height(24.dp))
        FashionistaRadarChart(breakdown = sampleVector)
        Spacer(Modifier.height(24.dp))
        FashionistaMathDecomposition(breakdown = FashionistaMathBreakdown())
        Spacer(Modifier.height(24.dp))
        FashionistaDecompositionBar(score = 87.0, breakdown = sampleVector)
    }
}
