package com.zoewave.probase.kocolor.features.analyzer.playlist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StylePlaylistScreen(
    viewModel: StylePlaylistViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "STYLE PLAYLIST",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9F9F9)
                )
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else if (uiState.currentPlaylist == null) {
            EmptyPlaylistState(onGenerate = { viewModel.onEvent(StylePlaylistEvent.GenerateWeekly) })
        } else {
            PlaylistContent(
                uiState = uiState,
                onCommit = { planId, ids -> viewModel.onEvent(StylePlaylistEvent.CommitDay(planId, ids)) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun EmptyPlaylistState(onGenerate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "NO PLAYLIST ACTIVE",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Generate your 7-day style forecast based on your unique color profile.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(
            onClick = onGenerate,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("GENERATE WEEKLY PLAN", modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun PlaylistContent(
    uiState: StylePlaylistUiState,
    onCommit: (String, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val playlist = uiState.currentPlaylist!!
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            PlaylistHeader(status = playlist.playlist.status)
        }
        
        items(playlist.dailyPlans.sortedBy { it.targetDate }) { plan ->
            DailyPlanCard(plan = plan, onCommit = { onCommit(plan.planId, plan.baseOutfitProductIds) })
        }
    }
}

@Composable
private fun PlaylistHeader(status: PlaylistStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "THIS WEEK",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Text(
                "Style Forecast",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Surface(
            color = if (status == PlaylistStatus.COMPLETED) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = status.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (status == PlaylistStatus.COMPLETED) Color(0xFF2E7D32) else Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun DailyPlanCard(
    plan: DailyStylePlanEntity,
    onCommit: () -> Unit
) {
    val isCommitted = plan.status == DailyPlanStatus.COMMITTED
    val dayName = plan.targetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US).uppercase()
    val dateStr = plan.targetDate.format(DateTimeFormatter.ofPattern("MMM dd"))

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(dayName, style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(dateStr, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }
                
                if (isCommitted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = plan.rationale.rotationReason ?: "Balanced selection for your rotation.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
            
            if (!isCommitted) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onCommit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black)
                ) {
                    Text("I'M WEARING THIS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
