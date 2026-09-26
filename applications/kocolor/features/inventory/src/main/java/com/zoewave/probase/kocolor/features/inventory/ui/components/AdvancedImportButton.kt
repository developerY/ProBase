package com.zoewave.probase.kocolor.features.inventory.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
    labelPrefix: String, // e.g. "COSMETICS VAULT" or "FASHION ARCHIVE"
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF4A2B42), // Deep aubergine matching the design
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                        text = "Open Advanced Import Options",
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


            }

            Spacer(modifier = Modifier.height(28.dp))

            // Method Icons Grid (2x2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
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
                labelPrefix = "FASHION ARCHIVE"
            )
        }
    }
}
