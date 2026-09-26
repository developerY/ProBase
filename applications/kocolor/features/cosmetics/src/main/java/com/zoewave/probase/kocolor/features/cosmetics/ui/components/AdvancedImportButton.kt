package com.zoewave.probase.kocolor.features.cosmetics.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdvancedImportButton(
    onBarcodeClick: () -> Unit,
    onProductClick: () -> Unit,
    onBoxClick: () -> Unit,
    onCatalogClick: () -> Unit,
    labelPrefix: String, // e.g. "COSMETICS VAULT" or "FASHION ARCHIVE"
    modifier: Modifier = Modifier,
    onExpandClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "ChevronRotation")

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF552C4E), // Top-left royal blackberry velvet
            Color(0xFF381B30),
            Color(0xFF1C071A)  // Bottom-right deep night plum
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.6.dp, Color(0xFFC6B492).copy(alpha = 0.45f)), // Metallic champagne perimeter bezel
        color = Color.Transparent,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row (Clickable to open/close card)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            expanded = !expanded
                            onExpandClick?.invoke()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Text Block
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Eyebrow with rose-gold square glyph
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFFC6B492), RoundedCornerShape(1.dp))
                            )
                            Text(
                                text = "$labelPrefix • 4 METHODS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.4.sp
                                ),
                                color = Color(0xFFC6B492) // Rose-gold tracked typography
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Action Headline
                        Text(
                            text = "Multi-Import Options",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            fontFamily = FontFamily.Serif,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Descriptor Subtitle
                        Text(
                            text = "Tap any method or expand to browse the full import suite",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // High-Affordance Interactive Sheet Trigger Disc (62px / ~56dp)
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.2.dp, Color(0xFFC6B492).copy(alpha = 0.5f)),
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.clip(CircleShape)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand Options",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .rotate(rotation)
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                // Horizontal bottom-sheet drag handle indicator line
                                Box(
                                    modifier = Modifier
                                        .width(12.dp)
                                        .height(2.dp)
                                        .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }

                // Collapsible 2x2 Grid Section
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Method Icons Grid (2x2)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MethodItem(
                                    icon = Icons.Default.QrCodeScanner,
                                    title = "Bar Scan",
                                    subtitle = "Scan UPC",
                                    iconColor = Color(0xFF9CA3AF),
                                    onClick = onBarcodeClick,
                                    modifier = Modifier.weight(1f)
                                )
                                MethodItem(
                                    icon = Icons.Default.PhotoCamera,
                                    title = "Product",
                                    subtitle = "5-Step AI",
                                    iconColor = Color(0xFFEC4899),
                                    onClick = onProductClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MethodItem(
                                    icon = Icons.Default.AutoAwesome,
                                    title = "Box Scan",
                                    subtitle = "7-Step AI",
                                    iconColor = Color(0xFFA1887F),
                                    onClick = onBoxClick,
                                    modifier = Modifier.weight(1f)
                                )
                                MethodItem(
                                    icon = Icons.Default.Storefront,
                                    title = "Catalog",
                                    subtitle = "Search OBF",
                                    iconColor = Color(0xFF10B981),
                                    onClick = onCatalogClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
    ) {
        // Text Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Icon Circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .drawBehind {
                    drawCircle(color = iconColor.copy(alpha = 0.15f), radius = size.minDimension / 2f)
                    drawCircle(
                        color = iconColor,
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 3f)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCF9F6)
@Composable
private fun AdvancedImportButtonPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AdvancedImportButton(
                onBarcodeClick = {},
                onProductClick = {},
                onBoxClick = {},
                onCatalogClick = {},
                labelPrefix = "COSMETICS VAULT"
            )
        }
    }
}
