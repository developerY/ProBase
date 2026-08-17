package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zoewave.probase.core.ui.util.PremiumProductImage
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ProductEditorialNotes

@Composable
fun ProductEditorialNotesDialog(
    notes: ProductEditorialNotes?,
    thumbnailUrl: String?,
    blurHash: String?,
    colorHex: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    if (isLoading || notes != null) {
        val itemColor = colorHex?.let { parseColor(it) } ?: Color.Transparent
        val backdropColor = Color(0xFFF3EAF2)

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(32.dp),
                color = backdropColor
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 750.dp)) {
                    // Not used for now. We will add it in the future.
                    /* 1. --- IMMERSIVE ATMOSPHERIC BACKGROUND BLUR ---
                    if (thumbnailUrl != null) {
                        PremiumProductImage(
                            imageUrl = thumbnailUrl,
                            blurHash = blurHash,
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .blur(45.dp) // Reduced from 60 for better chromatic presence
                                .alpha(0.3f), // Increased from 0.15 for visibility
                            contentScale = ContentScale.Crop,
                            fallbackColor = itemColor.copy(alpha = 0.2f)
                        )
                    }*/

                    // 2. --- DIALOG CONTENT ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Product Title
                        Text(
                            text = notes?.editorialTitle ?: "Analyzing Product...",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp,
                            color = Color(0xFF1A1C1E)
                        )
                        
                        Spacer(Modifier.height(24.dp))

                        // Scrollable Sections
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color(0xFF745E7A))
                                }
                            } else if (notes != null) {
                                // A. Visual Identity Card
                                if (thumbnailUrl != null) {
                                    EditorialCard(
                                        label = "Visual Identity",
                                        initiallyExpanded = true
                                    ) {
                                        Column {
                                            PremiumProductImage(
                                                imageUrl = thumbnailUrl,
                                                blurHash = blurHash,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .border(
                                                        BorderStroke(4.dp, itemColor.copy(alpha = 0.8f)),
                                                        RoundedCornerShape(12.dp)
                                                    ),
                                                contentScale = ContentScale.Crop,
                                                fallbackColor = itemColor
                                            )
                                            Spacer(Modifier.height(12.dp))
                                            Text(
                                                text = notes.editorialTitle.replace("Artist Notes: ", "").replace("Style Notes: ", ""),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = colorHex?.uppercase() ?: "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }

                                // B. Scientific Overview Card
                                notes.description?.let { desc ->
                                    EditorialCard(
                                        label = "Scientific Overview",
                                        initiallyExpanded = false
                                    ) {
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF333333),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }

                                // C. Dynamic Attributes
                                notes.attributes.forEach { attribute ->
                                    EditorialCard(
                                        label = attribute.label,
                                        initiallyExpanded = false
                                    ) {
                                        Text(
                                            text = attribute.body,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF333333),
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Close Action
                        TextButton(onClick = onDismiss) {
                            Text(
                                "CLOSE",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp),
                                color = Color(0xFF1A1C1E),
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorialCard(
    label: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable { isExpanded = !isExpanded },
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(500)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(500)) + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}
