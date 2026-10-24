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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            }
        }

        // Quick Dose Actions
        Text(
            text = "QUICK INTAKE",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B9198),
            fontFamily = FontFamily.Monospace
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onLogIntake(63.0) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242C34), contentColor = Color(0xFFE1E2E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Espresso (63mg)")
            }
            Button(
                onClick = { onLogIntake(35.0) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242C34), contentColor = Color(0xFFE1E2E9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Green Tea (35mg)")
            }
        }
    }
}
