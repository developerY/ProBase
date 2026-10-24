package com.zoewave.probase.goswift.mobile.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun HomeUiRoute(
    onNavigateToSync: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeUiScreen(
        uiState = uiState,
        onLogIntake = { viewModel.logIntake(it) },
        onNavigateToSync = onNavigateToSync
    )
}

@Composable
fun HomeUiScreen(
    uiState: HomeUiState,
    onLogIntake: (Double) -> Unit,
    onNavigateToSync: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GOSWIFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB4AA),
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Live Fuel",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFE1E2E9),
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF41474D))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFFF6F61), CircleShape)
                    )
                    Text(
                        text = "AI Active",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE1E2E9)
                    )
                }
            }
        }

        // Status Banner Card (Health Sync)
        Surface(
            onClick = onNavigateToSync,
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color(0xFFFDBA74),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Metabolic Sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE1E2E9),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Google Health Active • 0m ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B9198)
                        )
                    }
                }
                Surface(
                    color = Color(0xFF242C34),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "99.4% Fidelity",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFEF08A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Hero Metric - Caffeine Remaining Ring
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESTIMATED CAFFEINE REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B9198),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "OPTIMAL FLOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFDBA74),
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(180.dp)
                ) {
                    val progress = (uiState.remainingMg / uiState.limitMg).coerceIn(0.0, 1.0).toFloat()
                    Canvas(modifier = Modifier.size(160.dp)) {
                        drawArc(
                            color = Color(0xFF242C34),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFFFF6F61),
                            startAngle = 135f,
                            sweepAngle = 270f * progress,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%.1f".format(uiState.remainingMg),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFFE1E2E9),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "mg remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B9198)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Target Corridor",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B9198)
                        )
                        Text(
                            text = "35 — 50 mg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE1E2E9),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Half-life",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8B9198)
                        )
                        Text(
                            text = "4.8h",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE1E2E9),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Recommendation Card
        Surface(
            color = Color(0xFF1C2228),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFFFF6F61),
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Next Window: in 38m (2:15 PM)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE1E2E9),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recommended: 18mg microdose • Prevents 3:15 PM lull",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B9198)
                    )
                }
                Button(
                    onClick = { onLogIntake(18.0) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6F61),
                        contentColor = Color(0xFF101418)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Add 18mg", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // ML Adaptive Insight
        Surface(
            color = Color(0xFF161B20),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF242C34)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFFFEF08A),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Hummingbird ML Adaptive Insight",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFEF08A),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Cortisol naturally dipping post-lunch. A targeted 15–18mg microdose at 2:15 PM maintains steady state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE1E2E9)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFFFB4AA), modifier = Modifier.size(12.dp))
                        Text("Zero Jitter Risk", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(12.dp))
                        Text("Bedtime Cleared (10:30 PM)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                    }
                }
            }
        }

        // One-Tap Precision Ingestion Strip
        Text(
            text = "ONE-TAP PRECISION INGESTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B9198),
            fontFamily = FontFamily.Monospace
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onLogIntake(15.0) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2228), contentColor = Color(0xFFE1E2E9)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocalCafe, contentDescription = null, tint = Color(0xFFFF6F61), modifier = Modifier.size(16.dp))
                        Text("+15mg", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFF6F61), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Citrus Surge", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE1E2E9), fontWeight = FontWeight.SemiBold)
                    Text("Focus Pill", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }
            }
            Button(
                onClick = { onLogIntake(25.0) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2228), contentColor = Color(0xFFE1E2E9)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(16.dp))
                        Text("+25mg", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFDBA74), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Wild Berry", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE1E2E9), fontWeight = FontWeight.SemiBold)
                    Text("Cognitive Peak", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }
            }
            Button(
                onClick = { /* Water hydration logging */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C2228), contentColor = Color(0xFFE1E2E9)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFFFEF08A), modifier = Modifier.size(16.dp))
                        Text("+300ml", style = MaterialTheme.typography.labelMedium, color = Color(0xFFFEF08A), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("H₂O Hydrate", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE1E2E9), fontWeight = FontWeight.SemiBold)
                    Text("Clear Toxins", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }
            }
        }

        // Live Google Health Telemetry Bento Grid (2x2)
        Text(
            text = "LIVE GOOGLE HEALTH TELEMETRY",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B9198),
            fontFamily = FontFamily.Monospace
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            // Cardiovascular Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier
                    .weight(1f)
                    .alpha(1.0f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cardiovascular", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF6F61), modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("68", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("BPM", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("HRV 62ms • Stable Baseline", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB4AA))
                }
            }

            // Serum Glucose Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier
                    .weight(1f)
                    .alpha(0.5f) // Grayed out if continuous CGM stream is not active
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Serum Glucose", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("94", style = MaterialTheme.typography.headlineLarge, color = Color(0xFF8B9198), fontWeight = FontWeight.Bold)
                        Text("mg/dL", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("Steady • No Spike Detected", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            // Hydration Metric Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier
                    .weight(1f)
                    .alpha(1.0f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hydration Metric", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        Icon(Icons.Default.Opacity, contentDescription = null, tint = Color(0xFFFDBA74), modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("1,850", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE1E2E9), fontWeight = FontWeight.Bold)
                        Text("/ 2.4k ml", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("82% of optimal hydration", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFEF08A))
                }
            }

            // Circadian Energy Index Card
            Surface(
                color = Color(0xFF1C2228),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF242C34)),
                modifier = Modifier
                    .weight(1f)
                    .alpha(1.0f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Circadian Index", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198))
                        Icon(Icons.Default.SolarPower, contentDescription = null, tint = Color(0xFFFF6F61), modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("88", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFFFB4AA), fontWeight = FontWeight.Bold)
                        Text("/ 100", style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B9198), modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Text("Cognitive Zenith Active", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB4AA))
                }
            }
        }
    }
}
