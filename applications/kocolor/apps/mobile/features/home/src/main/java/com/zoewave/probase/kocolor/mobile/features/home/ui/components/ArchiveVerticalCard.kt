package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.ArchiveVerticalUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArchiveVerticalCard(
    uiState: ArchiveVerticalUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    val infiniteTransition = rememberInfiniteTransition(label = "HubActionPulseAnimation")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onEvent() },
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF3ECEF)
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            AsyncImage(
                model = uiState.imageModel,
                contentDescription = null,
                modifier = Modifier.matchParentSize().alpha(0.2f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Translucent frosted glass pill
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(86.dp),
                        shape = RoundedCornerShape(44.dp),
                        color = Color.White.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.title,
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (uiState.onDiscoverClick != null) {
                        // Floating Discover Badge (Intersecting top-right of pill)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (-12).dp)
                                .size(52.dp),
                            shape = CircleShape,
                            color = Color(0xFFEFE8E1).copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, Color.White),
                            shadowElevation = 4.dp,
                            onClick = uiState.onDiscoverClick
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(44.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = uiState.icon,
                                            contentDescription = uiState.discoverTitle ?: "Discover",
                                            tint = Color(0xFF1C1B1F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    // Small gold '+' add button badge overlapping the bottom right corner
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFFD700), // Gold
                                        border = BorderStroke(1.5.dp, Color(0xFFEFE8E1).copy(alpha = 0.95f)),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.BottomEnd)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add",
                                                tint = Color.Black,
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .graphicsLayer {
                                                        scaleX = pulseScale
                                                        scaleY = pulseScale
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // BOTTOM HALF: Items Tracked Row with Chevron (overlapping pill bottom edge)
                val chevronRotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "ChevronRotation"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .offset(y = (-14).dp)
                        .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.count} ${uiState.countLabel}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937),
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF8C7F72),
                        modifier = Modifier.size(20.dp).rotate(chevronRotation)
                    )
                }

                // Collapsible Content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TOTAL VALUE + AVG CPU / Health Metric 
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = uiState.valueLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = currencyFormatter.format(uiState.value),
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            
                            uiState.avgCpu?.let { cpu ->
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(50.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2E7D32))
                                        )
                                        Text(
                                            text = "AVG CPU: ${currencyFormatter.format(cpu)} / use",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.categoryProgress.isNotEmpty()) {
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 320.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                uiState.categoryProgress.forEach { progress ->
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(progress.color)
                                                )
                                                Text(
                                                    text = progress.label.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray,
                                                    fontSize = 8.sp,
                                                    letterSpacing = 0.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.Bottom,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                val percent = if (progress.target > 0) ((progress.owned.toFloat() / progress.target.toFloat()) * 100).toInt() else 0
                                                Text(
                                                    text = "$percent%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.End,
                                                    modifier = Modifier.width(36.dp)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.Bottom,
                                                    horizontalArrangement = Arrangement.End,
                                                    modifier = Modifier.width(32.dp)
                                                ) {
                                                    Text(
                                                        text = "${progress.owned}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black,
                                                        fontSize = 9.sp
                                                    )
                                                    Text(
                                                        text = "/${progress.target}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Gray,
                                                        fontSize = 7.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Progress Bar
                                        val fillRatio = if (progress.target > 0) (progress.owned.toFloat() / progress.target.toFloat()).coerceIn(0f, 1f) else 0f
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.08f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = fillRatio)
                                                    .fillMaxHeight()
                                                    .clip(CircleShape)
                                                    .background(progress.color)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.chromaticTone != null || uiState.healthMetric != null || uiState.restockMetric != null) {
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.chromaticTone?.let { tone ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF8E24AA), modifier = Modifier.size(14.dp))
                                        Row {
                                            Text(
                                                text = "Signature Tone: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = tone.removePrefix("Signature Tone: "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                uiState.healthMetric?.let { health ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(14.dp))
                                        Row {
                                            Text(
                                                text = "Vanity Health: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828),
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = health.removePrefix("Vanity Health: ").removeSuffix(" (PAO Alert)"),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                uiState.restockMetric?.let { restock ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFB8C00), modifier = Modifier.size(14.dp))
                                        Row {
                                            Text(
                                                text = "Restock needed: ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100),
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                text = restock.removePrefix("Restock needed: "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Vanity Card Preview")
@Composable
private fun ArchiveVerticalCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ArchiveVerticalCard(
                uiState = ArchiveVerticalUiState(
                    title = "Vanity",
                    count = 117,
                    countLabel = "items curated",
                    valueLabel = "TOTAL VALUE",
                    value = 7577.00,
                    imageModel = R.drawable.vanity_white_background,
                    icon = androidx.compose.material.icons.Icons.Default.Face,
                    discoverTitle = "DISCOVER",
                    discoverSubtitle = "Cosmetics",
                    discoverColor = Color(0xFF3D223B),
                    onDiscoverClick = {},
                    categoryProgress = listOf(
                        com.zoewave.probase.kocolor.mobile.features.home.ui.CategoryProgressItem("COMPLEXION", 58, 60, Color(0xFFD4AF37)),
                        com.zoewave.probase.kocolor.mobile.features.home.ui.CategoryProgressItem("EYES & BROWS", 20, 25, Color(0xFF4A2B4B))
                    ),
                    avgCpu = 1.20,
                    healthMetric = "2 items expiring this month (PAO Alert)",
                    restockMetric = "Restock needed: 2 formulas low",
                    chromaticTone = "Signature Tone: Roseate Sand · Warm/Matte"
                ),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
