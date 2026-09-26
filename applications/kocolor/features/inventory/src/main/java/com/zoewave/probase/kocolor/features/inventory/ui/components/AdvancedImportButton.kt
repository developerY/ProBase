package com.zoewave.probase.kocolor.features.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ExpandLess
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
    onExpandClick: () -> Unit,
    labelPrefix: String, // e.g. "COSMETICS VAULT" or "FASHION ARCHIVE"
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF381B30), // Deep aubergine matching the design
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "■ $labelPrefix • 4 METHODS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color(0xFFC6B492) // Gold tint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Advanced Import Options",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap any method or expand to browse the full import suite",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }


                // Expand Icon Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ExpandLess,
                        contentDescription = "Open",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp).rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(28.dp))

                    // Method Icons Grid (2x2)
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MethodItem(
                        icon = Icons.Default.QrCodeScanner,
                        title = "Bar Scan",
                        subtitle = "Scan UPC",
                        iconColor = Color(0xFF9CA3AF), // Lighter Gray for dark mode contrast
                        onClick = onBarcodeClick,
                        modifier = Modifier.weight(1f)
                    )
                    MethodItem(
                        icon = Icons.Default.PhotoCamera,
                        title = "Product",
                        subtitle = "5-Step AI",
                        iconColor = Color(0xFFEC4899), // Pink
                        onClick = onProductClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MethodItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Box Scan",
                        subtitle = "7-Step AI",
                        iconColor = Color(0xFFA1887F), // Lighter Brown
                        onClick = onBoxClick,
                        modifier = Modifier.weight(1f)
                    )
                    MethodItem(
                        icon = Icons.Default.Storefront,
                        title = "Catalog",
                        subtitle = "Search OBF",
                        iconColor = Color(0xFF10B981), // Green
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
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 14.dp)
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(40.dp)
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
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        // Text Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F0)
@Composable
private fun AdvancedImportButtonPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AdvancedImportButton(
                onBarcodeClick = {},
                onProductClick = {},
                onBoxClick = {},
                onCatalogClick = {},
                onExpandClick = {},
                labelPrefix = "FASHION ARCHIVE"
            )
        }
    }
}
