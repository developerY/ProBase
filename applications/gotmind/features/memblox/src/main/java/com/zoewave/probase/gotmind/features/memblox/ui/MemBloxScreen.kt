package com.zoewave.probase.gotmind.features.memblox.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import com.zoewave.probase.gotmind.features.memblox.HapticSignal
import com.zoewave.probase.gotmind.features.memblox.MatchGhost
import com.zoewave.probase.gotmind.features.memblox.MemBloxEvent
import com.zoewave.probase.gotmind.features.memblox.MemBloxState
import com.zoewave.probase.gotmind.features.memblox.PowerUpType
import com.zoewave.probase.gotmind.features.memblox.R
import com.zoewave.probase.gotmind.features.memblox.ScorePopup
import com.zoewave.probase.gotmind.model.MemBloxEngineType
import com.zoewave.probase.gotmind.model.memblox.MemBloxBlock
import com.zoewave.probase.gotmind.model.memblox.MemBloxDifficulty
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MemBloxScreen(
    uiState: MemBloxState,
    topScores: List<MemBloxScoreEntity>,
    engineType: MemBloxEngineType,
    onNav: (String) -> Unit,
    onEvent: (MemBloxEvent) -> Unit
) {
    var showAnalytics by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    // Haptic Feedback Observer
    LaunchedEffect(uiState.lastHapticSignal) {
        uiState.lastHapticSignal?.let { signal ->
            when (signal) {
                HapticSignal.LIGHT -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                HapticSignal.MEDIUM -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                HapticSignal.HEAVY -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onEvent(MemBloxEvent.HapticConsumed)
        }
    }

    if (!uiState.isStarted) {
        DifficultySelectionScreen(
            onDifficultySelected = { onEvent(MemBloxEvent.StartGame(it)) }
        )
        return
    }

    // Screenshake Offset
    val shakeX by animateFloatAsState(
        targetValue = if (uiState.shakeIntensity > 0 || uiState.isStressed) (Random.nextFloat() - 0.5f) * (uiState.shakeIntensity + 1f) * 10 else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "ShakeX"
    )
    val shakeY by animateFloatAsState(
        targetValue = if (uiState.shakeIntensity > 0 || uiState.isStressed) (Random.nextFloat() - 0.5f) * (uiState.shakeIntensity + 1f) * 10 else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "ShakeY"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        ParticleBackground(speedFactor = if (uiState.isFrenzy) 3f else 1f)

        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Ultra-Slim Header (Top Fixed HUD)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E1E).copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNav("BACK") },
                        modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_gotmind_features_memblox_back), modifier = Modifier.size(20.dp), tint = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.score.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (uiState.isFrenzy) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary)
                            Text(text = stringResource(R.string.applications_gotmind_features_memblox_score), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = stringResource(R.string.applications_gotmind_features_memblox_pairs_format, uiState.pairsMatched, uiState.targetPairs), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = stringResource(R.string.applications_gotmind_features_memblox_pairs), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    IconButton(
                        onClick = { showAnalytics = !showAnalytics },
                        modifier = Modifier.size(36.dp).background(if (showAnalytics) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = stringResource(R.string.applications_gotmind_features_memblox_analytics), modifier = Modifier.size(20.dp), tint = Color.White)
                    }
                }

                // Analytics HUD Overlay (Now strictly for statistics)
                AnimatedVisibility(visible = showAnalytics) {
                    Column(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E).copy(alpha = 0.95f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        val loadPct = if (uiState.cols * uiState.rows > 0) (uiState.peakBoardBlocks * 100 / (uiState.cols * uiState.rows)) else 0
                        AnalyticsRow(stringResource(R.string.applications_gotmind_features_memblox_hit_rate), stringResource(R.string.applications_gotmind_features_memblox_percent_format, (uiState.matchAccuracy * 100).toInt()))
                        AnalyticsRow(stringResource(R.string.applications_gotmind_features_memblox_best_streak), "${uiState.bestMatchStreak}")
                        AnalyticsRow(stringResource(R.string.applications_gotmind_features_memblox_avg_match_time), String.format(Locale.getDefault(), "%.1fs", uiState.avgMatchTimeMs / 1000f))
                        AnalyticsRow(stringResource(R.string.applications_gotmind_features_memblox_peak_board_load), "$loadPct%")
                        AnalyticsRow(stringResource(R.string.applications_gotmind_features_memblox_solubility), "${(uiState.successfulMatches * 100 / (uiState.totalClicks.coerceAtLeast(1)))}%")

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { 
                                    onEvent(MemBloxEvent.ResetToSelection)
                                    onNav("BACK") 
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.applications_gotmind_features_memblox_quit), style = MaterialTheme.typography.labelLarge)
                            }
                            
                            Button(
                                onClick = { onEvent(MemBloxEvent.TogglePause) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isPaused) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                    contentColor = if (uiState.isPaused) MaterialTheme.colorScheme.primary else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Icon(if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (uiState.isPaused) stringResource(R.string.applications_gotmind_features_memblox_resume) else stringResource(R.string.applications_gotmind_features_memblox_pause), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // 2. Progress Line
            val progress by animateFloatAsState(targetValue = uiState.pairsMatched.toFloat() / uiState.targetPairs, animationSpec = tween(500))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = if (uiState.isFrenzy) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Butt
            )

            // 3. Interactive Board Area
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
            ) {
                val blockSize = maxWidth / uiState.cols
                val blockHeight = maxHeight / uiState.rows

                uiState.grid.forEach { block ->
                    key(block.id) {
                        val nukingColor = uiState.nukingBlockIds[block.id]
                        val isHinted = uiState.hintedBlockIds.contains(block.id)
                        MemBloxBlockRender(
                            block = block,
                            blockSize = blockSize,
                            blockHeight = blockHeight,
                            isRevealed = uiState.isRevealed,
                            isInitiallyRevealed = uiState.initiallyRevealedBlockIds.contains(block.id),
                            isFallingMode = engineType == MemBloxEngineType.FALLING,
                            nukingColor = nukingColor?.let { Color(it) },
                            isHinted = isHinted,
                            dropHeight = uiState.dropHeight,
                            dropDurationMillis = uiState.dropDurationMillis,
                            onClick = { onEvent(MemBloxEvent.BlockClick(block)) }
                        )
                    }
                }

                if (uiState.isStressed) StressVignette()
                if (uiState.frostAlpha > 0) FrostOverlay(alpha = uiState.frostAlpha)
                if (uiState.isSlowed) Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFD54F).copy(alpha = 0.1f)))

                uiState.activeShockwaves.forEach { shock ->
                    key(shock.id) {
                        ShockwaveRenderer(centerX = blockSize * shock.col + (blockSize / 2), centerY = blockHeight * shock.row + (blockHeight / 2))
                    }
                }

                uiState.matchGhosts.forEach { ghost ->
                    key(ghost.id) {
                        MatchGhostRenderer(ghost = ghost, blockSize = blockSize, blockHeight = blockHeight)
                    }
                }

                uiState.confettiBursts.forEach { burst ->
                    key(burst.id) {
                        ConfettiBurstRenderer(centerX = blockSize * burst.col + (blockSize / 2), centerY = blockHeight * burst.row + (blockHeight / 2))
                    }
                }

                uiState.floatingTexts.forEach { effect ->
                    key(effect.id) {
                        FloatingTextRenderer(effect = effect, blockSize = blockSize, blockHeight = blockHeight)
                    }
                }

                uiState.floatingScores.forEach { popup ->
                    key(popup.id) {
                        ScorePopupRenderer(popup = popup, blockSize = blockSize, blockHeight = blockHeight)
                    }
                }

                // Combo & Frenzy Announcer
                if (uiState.combo > 1) {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 20.dp), contentAlignment = Alignment.TopCenter) {
                        Text(
                            text = if (uiState.isFrenzy) stringResource(R.string.applications_gotmind_features_memblox_frenzy) else stringResource(R.string.applications_gotmind_features_memblox_combo, uiState.multiplier),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (uiState.isFrenzy) Color(0xFFE91E63) else Color(0xFFFF5722),
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                if (uiState.isGameOver || uiState.isVictory) {
                    EndGameOverlay(
                        state = uiState, 
                        onRetry = { onEvent(MemBloxEvent.StartGame(uiState.difficulty)) }, 
                        onRetrySelection = { onEvent(MemBloxEvent.ResetToSelection) }
                    )
                }
            }

            // 4. Power-Up Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PowerUpType.entries.forEach { type ->
                    val count = uiState.powerUps[type] ?: 0
                    val isAvailable = count > 0 && !uiState.isGameOver && !uiState.isVictory && !uiState.isPaused
                    ElevatedAssistChip(
                        onClick = { onEvent(MemBloxEvent.UsePowerUp(type)) },
                        label = { Text("${stringResource(type.labelResId)} ($count)", style = MaterialTheme.typography.labelSmall, color = if (isAvailable) Color.White else Color.Gray) },
                        leadingIcon = { Text(type.icon, fontSize = 14.sp) },
                        enabled = isAvailable,
                        shape = RoundedCornerShape(24.dp),
                        colors = androidx.compose.material3.AssistChipDefaults.elevatedAssistChipColors(
                            containerColor = if (isAvailable) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f) else Color.Transparent,
                            disabledContainerColor = Color.Black.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.material3.AssistChipDefaults.assistChipBorder(enabled = isAvailable, borderColor = if (isAvailable) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f))
                    )
                }
            }
        }
    }
}

@Composable
fun ParticleBackground(speedFactor: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "Particles")
    val particles = remember { List(30) { RandomParticle() } }

    particles.forEach { p ->
        val yOffset by infiniteTransition.animateFloat(
            initialValue = p.startY,
            targetValue = p.startY + 2000f,
            animationSpec = infiniteRepeatable(
                animation = tween((p.duration / speedFactor).toInt(), easing = LinearEasing)
            ),
            label = "ParticleY"
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = p.color.copy(alpha = 0.2f),
                radius = p.size,
                center = androidx.compose.ui.geometry.Offset(p.startX, yOffset % size.height)
            )
        }
    }
}

data class RandomParticle(
    val startX: Float = Random.nextFloat() * 1000f,
    val startY: Float = Random.nextFloat() * 2000f,
    val size: Float = (2..6).random().toFloat(),
    val duration: Int = (10000..20000).random(),
    val color: Color = Color.White
)

@Composable
fun ShockwaveRenderer(centerX: Dp, centerY: Dp) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }
    Canvas(modifier = Modifier.offset(x = centerX, y = centerY).size(1.dp)) {
        val radius = progress.value * 1000f
        drawCircle(
            color = Color.White.copy(alpha = (1f - progress.value) * 0.5f),
            radius = radius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
        )
    }
}

@Composable
fun ScorePopupRenderer(popup: ScorePopup, blockSize: Dp, blockHeight: Dp) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1000, easing = LinearEasing))
    }
    val t = progress.value
    Box(
        modifier = Modifier
            .offset(x = blockSize * popup.col, y = blockHeight * popup.row - (t * 80).dp)
            .alpha(1f - t)
    ) {
        Text(
            text = stringResource(R.string.applications_gotmind_features_memblox_points_plus_format, popup.score),
            color = Color.Green,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun StressVignette() {
    val infiniteTransition = rememberInfiniteTransition(label = "Stress")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StressAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, Color.Red.copy(alpha = alpha), RoundedCornerShape(16.dp))
    )
}

@Composable
fun MatchGhostRenderer(ghost: MatchGhost, blockSize: Dp, blockHeight: Dp) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1000, easing = LinearEasing))
    }
    val t = progress.value
    Box(
        modifier = Modifier
            .size(blockSize, blockHeight)
            .offset(x = blockSize * ghost.col, y = blockHeight * ghost.row)
            .alpha((1f - t) * 0.5f)
            .scale(1f + t * 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Text(text = ghost.emoji, fontSize = (blockSize.value * 0.65).sp, color = Color.White)
    }
}

@Composable
fun FloatingTextRenderer(effect: com.zoewave.probase.gotmind.features.memblox.FloatingTextEffect, blockSize: Dp, blockHeight: Dp) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(1500, easing = LinearEasing))
    }
    val progress = t.value
    Box(
        modifier = Modifier
            .offset(x = blockSize * effect.col, y = blockHeight * effect.row - (progress * 100).dp)
            .alpha(1f - progress)
    ) {
        Text(
            text = stringResource(effect.textResId),
            color = Color(effect.color),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            modifier = Modifier.scale(1f + progress * 0.5f)
        )
    }
}

@Composable
fun FrostOverlay(alpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize().alpha(alpha)) {
        val frostColor = Color(0xFFE1F5FE)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, frostColor),
                center = center,
                radius = size.minDimension
            )
        )
        clipRect {
            drawRect(color = frostColor.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun ConfettiBurstRenderer(centerX: Dp, centerY: Dp) {
    val particles = remember {
        List(45) {
            ConfettiParticle(
                velocityX = (Math.random().toFloat() - 0.5f) * 600f,
                velocityY = (Math.random().toFloat() - 0.9f) * 900f,
                color = Color(
                    red = (150..255).random() / 255f,
                    green = (150..255).random() / 255f,
                    blue = (150..255).random() / 255f
                ),
                size = (4..8).random().toFloat()
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1200, easing = LinearEasing))
    }

    val t = progress.value
    Canvas(
        modifier = Modifier
            .offset(x = centerX, y = centerY)
            .size(1.dp) // Point of origin
    ) {
        particles.forEach { particle ->
            val x = particle.velocityX * t
            val y = particle.velocityY * t + 0.5f * 800f * t * t
            
            drawCircle(
                color = particle.color.copy(alpha = 1f - t),
                radius = particle.size * (1f - t * 0.5f),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

data class ConfettiParticle(
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float
)

@Composable
fun MemBloxBlockRender(
    block: MemBloxBlock,
    blockSize: Dp,
    blockHeight: Dp,
    isRevealed: Boolean,
    isInitiallyRevealed: Boolean = false,
    isFallingMode: Boolean = true,
    nukingColor: Color?,
    isHinted: Boolean = false,
    dropHeight: Int,
    dropDurationMillis: Int,
    onClick: () -> Unit
) {
    // Smooth Y Sliding with Animatable starting from above the board (if falling mode)
    val startY = if (isFallingMode) blockHeight.value * -dropHeight.toFloat() else blockHeight.value * block.row
    val animatedY = remember { Animatable(startY) }
    
    LaunchedEffect(block.row, blockHeight) {
        animatedY.animateTo(
            targetValue = blockHeight.value * block.row,
            animationSpec = tween(
                durationMillis = if (isFallingMode) dropDurationMillis else 0,
                easing = FastOutSlowInEasing
            )
        )
    }

    // A block is falling if it's visually significantly above its resting row
    val isFalling = isFallingMode && animatedY.value < (blockHeight.value * block.row) - 2f
    val isFlipped = block.isFlipped || isRevealed || isInitiallyRevealed || nukingColor != null || isFalling
    
    // 3D Flip Animation
    val animatedRotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "FlipRotation"
    )

    // Entrance & Idle Animations
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }
    
    val entranceScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "Entrance"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    // Click Flash Animation
    val flashAlpha = remember { Animatable(0f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HintPulse"
    )

    val baseColor = if (nukingColor != null) nukingColor else Color(block.color)
    val displayColor by animateColorAsState(
        targetValue = if (animatedRotationY >= 90f && nukingColor == null) Color.White else baseColor,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .size(blockSize, blockHeight)
            .offset(x = blockSize * block.col, y = animatedY.value.dp)
            .graphicsLayer {
                this.rotationY = animatedRotationY
                cameraDistance = 12f * density
            }
            .scale(entranceScale)
            .padding(1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (animatedRotationY >= 90f && nukingColor == null) {
                    Brush.linearGradient(listOf(Color.White, Color(0xFFF5F5F5)))
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            displayColor.copy(alpha = 0.9f),
                            displayColor.darken(0.2f)
                        )
                    )
                }
            )
            .drawWithContent {
                drawContent()
                // Gloss / Shimmer effect
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(size.width * shimmerOffset, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width * (shimmerOffset + 0.5f), size.height)
                )
                drawRect(brush = brush)
            }
            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .let { if (isHinted) it.border(2.dp, Color.Yellow.copy(alpha = hintAlpha), RoundedCornerShape(4.dp)) else it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { 
                scope.launch {
                    flashAlpha.snapTo(1f)
                    flashAlpha.animateTo(0f, tween(400))
                }
                onClick() 
            }
            .border(4.dp, Color.White.copy(alpha = flashAlpha.value), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (animatedRotationY >= 90f) {
            Text(
                text = block.emoji, 
                fontSize = (blockSize.value * 0.65).sp,
                modifier = Modifier.graphicsLayer { this.rotationY = 180f } // Fix mirror effect
            )
        }
    }
}

@Composable
fun EndGameOverlay(
    state: MemBloxState,
    onRetry: () -> Unit,
    onRetrySelection: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Rank Badge
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    color = when(state.finalRank) {
                        "S" -> Color(0xFFFFD700)
                        "A" -> Color(0xFFC0C0C0)
                        "B" -> Color(0xFFCD7F32)
                        else -> Color.Gray
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = state.finalRank,
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (state.isVictory) stringResource(R.string.applications_gotmind_features_memblox_victory) else stringResource(R.string.applications_gotmind_features_memblox_game_over),
                color = if (state.isVictory) Color.Green else Color.Red,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    EndGameStat(stringResource(R.string.applications_gotmind_features_memblox_final_score), state.score.toString())
                    EndGameStat(stringResource(R.string.applications_gotmind_features_memblox_best_streak), state.bestMatchStreak.toString())
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    EndGameStat(stringResource(R.string.applications_gotmind_features_memblox_hit_rate), "${(state.matchAccuracy * 100).toInt()}%")
                    EndGameStat(stringResource(R.string.applications_gotmind_features_memblox_avg_time), String.format(Locale.getDefault(), "%.1fs", state.avgMatchTimeMs / 1000f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.applications_gotmind_features_memblox_retry), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRetrySelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(stringResource(R.string.applications_gotmind_features_memblox_change_difficulty), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EndGameStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
fun AnalyticsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DifficultySelectionScreen(
    onDifficultySelected: (MemBloxDifficulty) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.applications_gotmind_features_memblox_game_title),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.applications_gotmind_features_memblox_game_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        MemBloxDifficulty.entries.forEach { difficulty ->
            DifficultyCard(difficulty, onDifficultySelected)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HallOfFameScreen(scores: List<MemBloxScoreEntity>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Info, 
                    contentDescription = stringResource(R.string.applications_gotmind_features_memblox_back), 
                    tint = Color.White
                )
            }
            Text(
                text = stringResource(R.string.applications_gotmind_features_memblox_hall_of_fame),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (scores.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.applications_gotmind_features_memblox_no_scores), color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(scores) { score ->
                    HallOfFameCard(score)
                }
            }
        }
    }
}

@Composable
fun HallOfFameCard(score: MemBloxScoreEntity) {
    val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(score.timestamp))
    
    // Medal Logic
    val isSniper = score.accuracy > 0.9f
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(score.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(score.score.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                }
                
                Row {
                    if (isSniper) MedalBadge(Icons.Default.TrackChanges, stringResource(R.string.applications_gotmind_features_memblox_medal_sniper), Color(0xFFFFC107))
                    if (score.bestStreak >= 8) MedalBadge(Icons.Default.Speed, stringResource(R.string.applications_gotmind_features_memblox_medal_streak), Color(0xFFE91E63))
                    if (score.powerUpsUsed == 0) MedalBadge(Icons.Default.Shield, stringResource(R.string.applications_gotmind_features_memblox_medal_pro), Color(0xFF03A9F4))
                }

                Text(date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.applications_gotmind_features_memblox_streak), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(score.bestStreak.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.applications_gotmind_features_memblox_hit_rate), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${(score.accuracy * 100).toInt()}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MedalBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp)
            .background(color.copy(alpha = 0.2f), CircleShape)
            .padding(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun DifficultyCard(difficulty: MemBloxDifficulty, onSelected: (MemBloxDifficulty) -> Unit) {
    val color = when (difficulty) {
        MemBloxDifficulty.EASY -> Color(0xFF4CAF50)
        MemBloxDifficulty.MEDIUM -> Color(0xFFFFC107)
        MemBloxDifficulty.EXPERT -> Color(0xFFF44336)
    }
    ElevatedCard(
        onClick = { onSelected(difficulty) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (difficulty) {
                        MemBloxDifficulty.EASY -> "🌱"
                        MemBloxDifficulty.MEDIUM -> "⚡"
                        MemBloxDifficulty.EXPERT -> "🔥"
                    },
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(difficulty.labelResId),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.applications_gotmind_features_memblox_board_info, difficulty.cols, difficulty.rows, difficulty.targetPairs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f))
        }
    }
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}
