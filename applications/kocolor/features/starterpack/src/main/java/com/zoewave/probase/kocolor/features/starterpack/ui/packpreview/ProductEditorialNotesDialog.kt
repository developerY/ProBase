package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
        val backdropColor = Color(0xFFF3EAF2) // Signature lavender backdrop

        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notes?.editorialTitle ?: "Analyzing Product...",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-0.5).sp,
                        color = Color(0xFF1A1C1E)
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF745E7A))
                        }
                    } else if (notes != null) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Visual Identity Card
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
                                            text = notes.editorialTitle.replace("Artist Notes: ", ""),
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

                            // 2. Scientific Overview Card
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

                            // 3. Dynamic Attributes Cards
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
                }
            },
            confirmButton = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
            },
            shape = RoundedCornerShape(32.dp),
            containerColor = backdropColor
        )
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
            .clip(RoundedCornerShape(20.dp))
            .clickable { isExpanded = !isExpanded },
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}
