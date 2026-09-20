package com.zoewave.probase.kocolor.features.cosmetics.ui.components

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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun VanityInventoryCard(
    totalItems: Int,
    totalValue: Double,
    expiringCount: Int,
    onViewInventoryClicked: () -> Unit,
    onViewExpiringClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val infiniteTransition = rememberInfiniteTransition(label = "WarningPulseAnimation")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFFF3F3F7)
    ) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP HALF: Main Glassmorphic Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewInventoryClicked() }
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Kitchen,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "PERSONAL ARCHIVE",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp,
                                            color = Color.Black,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "The Vanity Collection",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Floating Expiring Badge (Intersecting top-right of pill)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = (-12).dp)
                            .size(52.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFEBEE), // Light red
                        border = BorderStroke(1.dp, Color.White),
                        shadowElevation = 4.dp,
                        onClick = onViewExpiringClicked
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFC62828).copy(alpha = 0.4f), CircleShape)
                            )
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Expiring Soon",
                                tint = Color(0xFFC62828),
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        if (expiringCount > 0) {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        }
                                    }
                            )
                            
                            if (expiringCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFC62828),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp)
                                ) {
                                    Text(
                                        text = expiringCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
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
                        text = "$totalItems ITEMS IN INVENTORY",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
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
                        Surface(color = if (expiringCount > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = if (expiringCount > 0) "ACTION NEEDED" else "HEALTHY STATUS", 
                                color = if (expiringCount > 0) Color(0xFFC62828) else Color(0xFF2E7D32), 
                                style = MaterialTheme.typography.labelSmall, 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                                fontSize = 9.sp
                            )
                        }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Value", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 11.sp)
                                Text(currencyFormatter.format(totalValue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Items Expiring Soon", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 11.sp)
                                Text("$expiringCount items", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (expiringCount > 0) Color(0xFFC62828) else Color.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Avg Item Value", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 11.sp)
                                val avgItemValue = if(totalItems > 0) totalValue / totalItems else 0.0
                                Text(currencyFormatter.format(avgItemValue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onViewInventoryClicked() }
                        ) {
                            Text(
                                text = "VIEW FULL INVENTORY ->",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Vanity Inventory Card Preview")
@Composable
private fun VanityInventoryCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VanityInventoryCard(
                totalItems = 175,
                totalValue = 2450.00,
                expiringCount = 2,
                onViewInventoryClicked = {},
                onViewExpiringClicked = {}
            )
        }
    }
}
