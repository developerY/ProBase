package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shared Section Header
        SectionTitle(
            uiState = SectionTitleUiState(
                title = "Unified Offering Archive",
                subtitle = "LIVE INDEX"
            )
        )

        // Main Glassmorphic Collection Hub Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { navTo(KoColorRoute.CollectionHub) },
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF3ECEF)
        ) {
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                AsyncImage(
                    model = R.drawable.collection_hub_background,
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
                                    text = "Hub Collection",
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Floating Fashion Advisor Badge (Intersecting top-right of pill, matching Weather/Routine badges)
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (-12).dp)
                                .size(52.dp),
                            shape = CircleShape,
                            color = Color(0xFFEFE8E1).copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, Color.White),
                            shadowElevation = 4.dp,
                            onClick = { navTo(KoColorRoute.StyleSimulator) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color(0xFFC6B492).copy(alpha = 0.4f), CircleShape)
                                )
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Fashion Advisor",
                                    tint = Color(0xFF1C1B1F),
                                    modifier = Modifier.size(20.dp)
                                )
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
                            text = "$totalItems items tracked",
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

                    // Collapsible Content (Total Value & Navigation Action)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
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
                                modifier = Modifier.size(56.dp),
                                onClick = { navTo(KoColorRoute.CollectionHub) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Collection Hub",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Black
                                    )
                                }
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

@Preview(showBackground = true)
@Composable
private fun CollectionHubCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CollectionHubCard(
                uiState = HomeUiState(),
                onEvent = {},
                navTo = {}
            )
        }
    }
}
