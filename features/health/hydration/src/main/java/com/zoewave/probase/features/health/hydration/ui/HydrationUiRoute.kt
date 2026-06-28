package com.zoewave.probase.features.health.hydration.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zoewave.probase.features.health.hydration.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HydrationUiRoute(
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HydrationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HydrationUiRoute(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSettings = onNavigateToSettings,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
internal fun HydrationUiRoute(
    uiState: HydrationUiState,
    onEvent: (HydrationUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HydrationScreen(
        uiState = uiState,
        onEvent = onEvent,
        onNavigateToSettings = onNavigateToSettings,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(
    uiState: HydrationUiState,
    onEvent: (HydrationUiEvent) -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.features_health_hydration_title), 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.features_health_hydration_cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(HydrationUiEvent.ResetProgress) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.features_health_hydration_cd_reset))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE1F5FE), Color.White)
                    )
                )
                .padding(padding)
        ) {
            val progress = (uiState as? HydrationUiState.Success)?.let { 
                (it.dailyTotalLiters / it.targetLiters).toFloat().coerceIn(0f, 1f)
            } ?: 0f
            
            // Spectacular Wave Background
            com.zoewave.probase.features.health.hydration.ui.components.WavyLiquidEngine(progress = progress)

            when (uiState) {
                HydrationUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HydrationUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // 1. Large Volume Display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "%.1fL".format(uiState.dailyTotalLiters),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF1A1A1A)
                            )
                            Surface(
                                onClick = onNavigateToSettings,
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.features_health_hydration_goal_format, uiState.targetLiters),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Settings, 
                                        null, 
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        // 2. Quick Log Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QuickHydrationButton(
                                label = stringResource(R.string.features_health_hydration_add_glass),
                                subLabel = stringResource(R.string.features_health_hydration_label_glass),
                                icon = Icons.Rounded.LocalCafe,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HydrationUiEvent.AddWater(0.25)) }
                            )
                            QuickHydrationButton(
                                label = stringResource(R.string.features_health_hydration_add_bottle),
                                subLabel = stringResource(R.string.features_health_hydration_label_bottle),
                                icon = Icons.Rounded.WineBar,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(HydrationUiEvent.AddWater(0.5)) }
                            )
                        }

                        // 3. Custom Amount Pill - Clean Frosted
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .clickable { /* Logic for custom slider popup */ },
                            color = Color.White.copy(alpha = 0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color.Black.copy(alpha = 0.6f))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.features_health_hydration_custom_amount), fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color.Black.copy(alpha = 0.7f))
                                }
                            }
                        }

                        // 4. Reminder Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.features_health_hydration_reminder),
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = Color.White.copy(alpha = 0.3f)
                            ) {
                                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(stringResource(R.string.features_health_hydration_smart_alerts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Switch(
                                            checked = uiState.isSmartAlertsEnabled, 
                                            onCheckedChange = { onEvent(HydrationUiEvent.ToggleSmartAlerts(it)) }
                                        )
                                    }
                                    Text(
                                        text = stringResource(R.string.features_health_hydration_smart_alerts_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    
                                    if (uiState.isSmartAlertsEnabled && uiState.nextReminderTime != null) {
                                        HorizontalDivider(modifier = Modifier.alpha(0.1f))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Settings, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.features_health_hydration_next_interval, uiState.nextReminderTime),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Recent Logs
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.features_health_hydration_recent_logs),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.Gray
                            )
                            
                            uiState.recentLogs.take(5).forEach { log ->
                                HydrationLogItem(log)
                            }
                        }
                        
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickHydrationButton(
    label: String,
    subLabel: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        color = Color.White.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.Black.copy(alpha = 0.6f))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, color = Color.Black.copy(alpha = 0.7f))
                Text(subLabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black), color = Color.Black.copy(alpha = 0.35f), letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun HydrationLogItem(log: HydrationLog) {
    val time = Instant.ofEpochMilli(log.timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
        
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFF3E5F5).copy(alpha = 0.8f) // Lavender background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.features_health_hydration_ml_format, (log.amountLiters * 1000).toInt()), 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(time, style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = 0.6f))
        }
    }
}
