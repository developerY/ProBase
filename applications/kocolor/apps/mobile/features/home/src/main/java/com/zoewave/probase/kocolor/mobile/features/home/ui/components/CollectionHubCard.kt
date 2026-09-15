package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.mobile.features.home.ui.HomeUiState
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.NumberFormat
import java.util.Locale

@Preview(showBackground = true)
@Composable
private fun CollectionHubCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CollectionHubCard(
                uiState = HomeUiState(
                    totalVanityValue = 1250.0,
                    totalWardrobeValue = 3400.0,
                    totalCosmetics = 12,
                    totalClothing = 24
                ),
                onEvent = {},
                navTo = {}
            )
        }
    }
}

@Composable
fun CollectionHubCard(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val totalValue = uiState.totalVanityValue + uiState.totalWardrobeValue
    val totalItems = uiState.totalCosmetics + uiState.totalClothing

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Archive Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UNIFIED OFFERING ARCHIVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = Color.Gray
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6B3A8B))
                )
                Text(
                    text = "LIVE INDEX",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B3A8B),
                    letterSpacing = 1.sp
                )
            }
        }

        // Main Collection Hub Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            shape = RoundedCornerShape(32.dp),
            onClick = { navTo(KoColorRoute.CollectionHub) }
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = R.drawable.collection_hub_background,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize().alpha(0.2f),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Top Row: "Hub" + Subtitle on Left, Fashion Advisor AI on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Hub",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$totalItems items\ntracked",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                lineHeight = 18.sp
                            )
                        }

                        // Fashion Advisor AI Card (Top Right)
                        Surface(
                            onClick = { navTo(KoColorRoute.StyleSimulator) },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF8F3FA),
                            border = BorderStroke(1.dp, Color(0xFFECE4EE))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEADBf4),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF6B3A8B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Fashion Advisor",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFF5A3854)
                                        ) {
                                            Text(
                                                text = "AI",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Visual Styling",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)

                    // Bottom Row: Total Value & Big Arrow Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "TOTAL VALUE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = currencyFormatter.format(totalValue),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        }

                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subtext Below Card
        Text(
            text = "KoColor Atelier • Curated Personal Archive",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
