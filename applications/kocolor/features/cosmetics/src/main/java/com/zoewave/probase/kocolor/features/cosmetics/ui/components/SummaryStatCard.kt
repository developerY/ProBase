package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.kocolor.features.cosmetics.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isExpiring = label.contains("EXPIRING")
    
    val serifFont = FontFamily.Serif
    val charcoal = Color(0xFF2C2420)
    
    val valueBrush = if (isExpiring) {
        Brush.linearGradient(listOf(Color(0xFF8E5431), Color(0xFFD4AF37), Color(0xFF8E5431)))
    } else {
        null
    }

    val actionBg = if (isExpiring) {
        Brush.linearGradient(listOf(Color(0xFF4A0000), Color(0xFF8B0000), Color(0xFF4A0000)))
    } else {
        Brush.linearGradient(listOf(
            Color(0xFFA0C4FF), Color(0xFFBDB2FF), Color(0xFFFFADAD), 
            Color(0xFFFFD6A5), Color(0xFFFDFFB6), Color(0xFFCAFFBF)
        ))
    }
    
    val actionText = if (isExpiring) stringResource(R.string.applications_kocolor_features_cosmetics_view_items) else stringResource(R.string.applications_kocolor_features_cosmetics_view_blueprint)
    val actionContentColor = if (isExpiring) Color.White else charcoal.copy(alpha = 0.8f)

    val glassBg = if (isExpiring) {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF0F0)))
    } else {
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F7FF)))
    }

    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(glassBg)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = charcoal.copy(alpha = 0.4f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (valueBrush != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontFamily = serifFont,
                            brush = valueBrush
                        ),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontFamily = serifFont
                        ),
                        fontWeight = FontWeight.Bold,
                        color = charcoal
                    )
                }
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    ),
                    color = charcoal.copy(alpha = 0.5f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(actionBg)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White.copy(alpha = if (isExpiring) 0.12f else 0.45f), 
                    border = BorderStroke(0.5.dp, actionContentColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Black,
                            color = actionContentColor,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = actionContentColor
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SummaryStatCardPreview() {
    MaterialTheme {
        SummaryStatCard(
            label = "TOTAL PRODUCTS",
            value = "34",
            icon = Icons.Default.Inventory2,
            onClick = {}
        )
    }
}
