package com.zoewave.probase.gotmind.features.leaderboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import com.zoewave.probase.gotmind.database.MindWaveScoreEntity
import com.zoewave.probase.gotmind.features.leaderboard.R
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    membloxScores: List<MemBloxScoreEntity>,
    mindwaveScores: List<MindWaveScoreEntity>,
    onBack: () -> Unit = {},
    onClearMemBlox: () -> Unit = {},
    onClearMindWave: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                    contentDescription = null, 
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.applications_gotmind_features_leaderboard_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            val currentScores = if (selectedTab == 0) membloxScores else mindwaveScores
            if (currentScores.isNotEmpty()) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("MEMBLOX", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("SOUNDMIND", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val currentList = if (selectedTab == 0) membloxScores else mindwaveScores
        if (currentList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.applications_gotmind_features_leaderboard_empty),
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(currentList) { score ->
                    if (score is MemBloxScoreEntity) {
                        HallOfFameCard(score)
                    } else if (score is MindWaveScoreEntity) {
                        MindWaveHallOfFameCard(score)
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.Gray,
            title = { Text(stringResource(R.string.applications_gotmind_features_leaderboard_clear)) },
            text = { Text(stringResource(R.string.applications_gotmind_features_leaderboard_clear_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedTab == 0) onClearMemBlox() else onClearMindWave()
                    showDeleteConfirm = false
                }) {
                    Text("CLEAR ALL", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun HallOfFameCard(score: MemBloxScoreEntity) {
    val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(score.timestamp))
    val isSniper = score.accuracy > 0.9f

    val difficultyResId = MemBloxDifficulty.entries.find { it.name == score.difficulty }?.labelResId
        ?: com.zoewave.probase.gotmind.model.R.string.applications_gotmind_model_diff_expert

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = stringResource(difficultyResId), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(score.score.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                }

                Row {
                    if (isSniper) MedalBadge(Icons.Default.TrackChanges, stringResource(R.string.applications_gotmind_features_leaderboard_medal_sniper), Color(0xFFFFC107))
                    if (score.bestStreak >= 8) MedalBadge(Icons.Default.Speed, stringResource(R.string.applications_gotmind_features_leaderboard_medal_streak), Color(0xFFE91E63))
                    if (score.powerUpsUsed == 0) MedalBadge(Icons.Default.Shield, stringResource(R.string.applications_gotmind_features_leaderboard_medal_pro), Color(0xFF03A9F4))
                }

                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.applications_gotmind_features_leaderboard_peak_streak), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(score.bestStreak.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.applications_gotmind_features_leaderboard_hit_rate), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = stringResource(R.string.applications_gotmind_features_leaderboard_percent_format, (score.accuracy * 100).toInt()), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MindWaveHallOfFameCard(score: MindWaveScoreEntity) {
    val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(score.timestamp))
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "LEVEL ${score.level}", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.secondary, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(score.score.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                }
                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun MedalBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Box(modifier = Modifier.padding(start = 6.dp).background(color.copy(alpha = 0.2f), CircleShape).padding(6.dp)) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(18.dp))
    }
}
