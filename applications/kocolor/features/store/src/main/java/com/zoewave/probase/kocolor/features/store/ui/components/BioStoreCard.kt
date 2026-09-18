package com.zoewave.probase.kocolor.features.store.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.store.R
import com.zoewave.probase.kocolor.features.store.ui.StoreEvent
import com.zoewave.probase.kocolor.features.store.ui.StoreUiState
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun BioStoreCard(
    uiState: StoreUiState,
    onEvent: (StoreEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                android.util.Log.d("BioStoreCard", "BioStoreCard Clicked -> Navigating to Store")
                onEvent(StoreEvent.EnterStore) 
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
        ) {

                // Background Image
                AsyncImage(
                    model = R.drawable.applications_kocolor_features_store_kocolor_store_front,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    alpha = 0.5f
                )

                // The central frosted glass panel
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.65f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Subtitle Row with Flanking Lines: — ATELIER BOUTIQUE — (Centered)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.width(28.dp),
                                color = Color(0xFF8C5A46).copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_store_boutique_title).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 2.sp,
                                color = Color(0xFF8C5A46),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.width(28.dp),
                                color = Color(0xFF8C5A46).copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                        }

                        // Main Display Title: THE ART OF COLOR (Centered)
                        Text(
                            text = stringResource(R.string.applications_kocolor_features_store_boutique_subtitle).uppercase(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 27.sp,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.White.copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            fontFamily = FontFamily.Serif,
                            color = Color(0xB28C523E), // Dark bronze
                            textAlign = TextAlign.Center
                        )

                        // Body Description (Centered)
                        Text(
                            text = "Curated boutique - Science meets aesthetics",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8C5A46),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            //modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Spacer(Modifier.height(12.dp))

                        // Bottom Pill Button (Centered)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, Color(0xFFBCA69C).copy(alpha = 0.6f)),
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "KoColor Store",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B3A2C),
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF6B3A2C),
                                    modifier = Modifier.size(14.dp).rotate(-45f)
                                )
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
                                                    text = "${percent}%",
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

                }
            }
        }
    }
}
    }
}

@Preview(showBackground = true)
@Composable
private fun BioStoreCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BioStoreCard(
                uiState = StoreUiState(),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
