package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SummaryStatUiState(
    val label: String,
    val value: String,
    val icon: ImageVector
)

@Composable
fun SummaryStatCard(
    uiState: SummaryStatUiState,
    modifier: Modifier = Modifier,
    onEvent: () -> Unit
) {
    val isExpiring = uiState.label.contains("EXPIRING")
    val serifFont = FontFamily.Serif
    val charcoal = Color(0xFF2C2420)

    val chromaticBrush = if (isExpiring) {
        Brush.verticalGradient(listOf(Color(0xFF4A0000), Color(0xFF8B0000)))
    } else {
        Brush.horizontalGradient(listOf(
            Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD), 
            Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
        ))
    }

    val valueBrush = if (isExpiring) {
        Brush.linearGradient(listOf(Color(0xFF8E5431), Color(0xFFD4AF37), Color(0xFF8E5431)))
    } else {
        null
    }

    Surface(
        onClick = onEvent,
        modifier = modifier.height(80.dp), // Half-height pill
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                // High-Visibility Icon (Top Left)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.12f))
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = uiState.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray.copy(alpha = 0.7f)
                    )
                }

                // Centered Number
                if (valueBrush != null) {
                    Text(
                        text = uiState.value,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = serifFont,
                            brush = valueBrush,
                            fontSize = 32.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Text(
                        text = uiState.value,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = serifFont,
                            fontSize = 32.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = charcoal,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // 2. Bottom Colored Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(chromaticBrush),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = if (isExpiring) 0.12f else 0.45f), 
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpiring) "VIEW ITEMS" else "VIEW INVENTORY",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            fontWeight = FontWeight.Black,
                            color = if (isExpiring) Color.White else charcoal.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp),
                            tint = if (isExpiring) Color.White else charcoal.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
