package com.zoewave.probase.seaweed.features.spendingcontrol.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.util.CurrencyUtils
import com.zoewave.probase.seaweed.features.spendingcontrol.domain.Envelope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvelopeManagementScreen(
    onBack: () -> Unit,
    viewModel: EnvelopeViewModel = hiltViewModel()
) {
    val envelopes by viewModel.envelopes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Control", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                EnvelopeExplainerHeader()
            }

            item {
                Text(
                    text = "Active Envelopes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (envelopes.isEmpty()) {
                item {
                    EmptyEnvelopesPlaceholder()
                }
            } else {
                items(envelopes, key = { it.id }) { envelope ->
                    EnvelopeCard(envelope = envelope)
                }
            }

            item {
                PhilosophySection()
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EnvelopeExplainerHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Real-Time Enforcement",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Envelopes are not just tracking. They are boundaries. Every transaction is checked before it is saved. If you exceed your limit, we intervene.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun EnvelopeCard(envelope: Envelope) {
    val progress = if (envelope.monthlyLimitCents > 0) {
        (envelope.currentSpentCents.toFloat() / envelope.monthlyLimitCents).coerceIn(0f, 1f)
    } else 0f
    
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val isNearLimit = progress > 0.9f
    val remaining = envelope.monthlyLimitCents - envelope.currentSpentCents

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = if (isNearLimit) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = envelope.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${envelope.categoryIds.size} Categories linked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = CurrencyUtils.formatCents(remaining) + " left",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isNearLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (isNearLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Spent: ${CurrencyUtils.formatCents(envelope.currentSpentCents)}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Limit: ${CurrencyUtils.formatCents(envelope.monthlyLimitCents)}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun PhilosophySection() {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Seaweed Philosophy",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        PhilosophyItem(
            icon = Icons.Default.Speed,
            title = "Zero-Latency Decision",
            description = "We use local rules and cached models to decide instantly. Your payment flow is never blocked by network calls."
        )
        
        PhilosophyItem(
            icon = Icons.Default.Info,
            title = "Transparent Friction",
            description = "We don't just say 'no'. We show you why and offer you the override path immediately."
        )
        
        PhilosophyItem(
            icon = Icons.Default.AutoGraph,
            title = "Adaptive Boundaries",
            description = "The system learns from your overrides and suggests adjustments that actually fit your life."
        )
    }
}

@Composable
private fun PhilosophyItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyEnvelopesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Initializing secure boundaries...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
