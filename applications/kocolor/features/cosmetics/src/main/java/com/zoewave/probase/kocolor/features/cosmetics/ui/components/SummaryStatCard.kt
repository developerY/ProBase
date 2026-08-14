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
    val isExpiring = uiState.label.contains("EXPIRING") || uiState.label.contains("Soon")
    val serifFont = FontFamily.Serif
    val charcoal = Color(0xFF2C2420)

    val footerBrush = if (isExpiring) {
        Brush.verticalGradient(listOf(Color(0xFF4A101D), Color(0xFF1A050A))) // Dark Burgundy/Plum
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
        modifier = modifier.height(180.dp), // Taller card as per image
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Icon and Value Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // High-Visibility Icon
                Icon(
                    imageVector = uiState.icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isExpiring) Color(0xFFB76E79) else charcoal
                )

                Spacer(Modifier.height(8.dp))

                // Centered Number
                if (valueBrush != null) {
                    Text(
                        text = uiState.value,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = serifFont,
                            brush = valueBrush,
                            fontSize = 48.sp
                        ),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = uiState.value,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = serifFont,
                            fontSize = 48.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = charcoal
                    )
                }
            }

            // 2. Bottom Colored Footer Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(footerBrush),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpiring) "VIEW ITEMS" else "VIEW INVENTORY",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Black,
                        color = if (isExpiring) Color.White else charcoal.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isExpiring) Color.White else charcoal.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
