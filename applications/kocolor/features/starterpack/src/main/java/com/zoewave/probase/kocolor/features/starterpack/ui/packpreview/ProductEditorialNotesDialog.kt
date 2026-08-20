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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    isInCart: Boolean,
    isOwned: Boolean,
    onBuy: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isLoading || notes != null) {
        val itemColor = colorHex?.let { parseColor(it) } ?: Color.Transparent
        val backdropColor = Color(0xFFF3EAF2)

        val buttonLabel = when {
            isOwned -> "OWNED"
            isInCart -> "IN CART"
            else -> "BUY"
        }

        val buttonIcon = when {
            isOwned -> Icons.Filled.Inventory
            isInCart -> Icons.Filled.Check
            else -> null
        }

        val buttonEnabled = !isOwned

        // --- EXPANSION STATE MANAGEMENT ---
        val visualIdentityLabel = "Visual Identity"
        val descriptionLabel = "Description"
        val scientificOverviewLabel = "Scientific Overview"
        
        var expandedLabels by remember { mutableStateOf(setOf(visualIdentityLabel)) }

        val allLabels = remember(notes) {
            val dynamic = notes?.attributes?.map { it.label }.orEmpty()
            setOf(visualIdentityLabel, descriptionLabel, scientificOverviewLabel) + dynamic
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(32.dp),
                color = backdropColor,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                shadowElevation = 12.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 850.dp)) {
                    
                    // 1. --- IMMERSIVE ATMOSPHERIC BACKGROUND BLUR ---
                    if (thumbnailUrl != null) {
                        PremiumProductImage(
                            imageUrl = thumbnailUrl,
                            blurHash = blurHash,
                            contentDescription = null,
                            modifier = Modifier
                                .matchParentSize()
                                .blur(60.dp)
                                .alpha(0.35f),
                            contentScale = ContentScale.Crop,
                            fallbackColor = itemColor.copy(alpha = 0.2f)
                        )
                    }

                    // 2. --- DIALOG CONTENT ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header: Title with premium Serif Font
                        Text(
                            text = notes?.editorialTitle ?: "Analyzing Product...",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp,
                            color = Color(0xFF1A1C1E)
                        )

                        // EXPAND / COLLAPSE ALL ACTIONS
                        if (notes != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { expandedLabels = allLabels }) {
                                    Text("EXPAND ALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                                Text(
                                    " • ", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = Color.Gray.copy(alpha = 0.5f)
                                )
                                TextButton(onClick = { expandedLabels = emptySet() }) {
                                    Text("COLLAPSE ALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }
                        } else {
                            Spacer(Modifier.height(24.dp))
                        }

                        // Scrollable Modular Card Stack
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
                                // A. VISUAL IDENTITY CARD
                                if (thumbnailUrl != null) {
                                    EditorialCard(
                                        label = visualIdentityLabel,
                                        isExpanded = expandedLabels.contains(visualIdentityLabel),
                                        onToggle = { 
                                            expandedLabels = if (expandedLabels.contains(visualIdentityLabel)) expandedLabels - visualIdentityLabel else expandedLabels + visualIdentityLabel
                                        }
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
                                            
                                            Spacer(Modifier.height(16.dp))
                                            
                                            // Product Sub-header & Hex DNA
                                            Text(
                                                text = notes.editorialTitle.replace("Artist Notes: ", "").replace("Style Notes: ", ""),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = colorHex?.uppercase() ?: "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray,
                                                letterSpacing = 1.sp
                                            )

                                            // Marketing Summary (Hook)
                                            notes.summary?.let { summary ->
                                                Spacer(Modifier.height(12.dp))
                                                Text(
                                                    text = summary,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.DarkGray,
                                                    lineHeight = 22.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // B. BRAND NARRATIVE CARD (Description from description.md)
                                notes.description?.let { desc ->
                                    EditorialCard(
                                        label = descriptionLabel,
                                        isExpanded = expandedLabels.contains(descriptionLabel),
                                        onToggle = { 
                                            expandedLabels = if (expandedLabels.contains(descriptionLabel)) expandedLabels - descriptionLabel else expandedLabels + descriptionLabel
                                        }
                                    ) {
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF333333),
                                            lineHeight = 22.sp
                                        )
                                    }
                                }

                                // C. SCIENTIFIC OVERVIEW CARD (Technical from Product_Description.md)
                                notes.technicalOverview?.let { tech ->
                                    EditorialCard(
                                        label = scientificOverviewLabel,
                                        isExpanded = expandedLabels.contains(scientificOverviewLabel),
                                        onToggle = { 
                                            expandedLabels = if (expandedLabels.contains(scientificOverviewLabel)) expandedLabels - scientificOverviewLabel else expandedLabels + scientificOverviewLabel
                                        }
                                    ) {
                                        Text(
                                            text = tech,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF333333),
                                            lineHeight = 22.sp
                                        )
                                    }
                                }

                                // D. DYNAMIC ATTRIBUTE CARDS (Usage, Expert Tips, etc.)
                                notes.attributes.forEach { attribute ->
                                    EditorialCard(
                                        label = attribute.label,
                                        isExpanded = expandedLabels.contains(attribute.label),
                                        onToggle = { 
                                            expandedLabels = if (expandedLabels.contains(attribute.label)) expandedLabels - attribute.label else expandedLabels + attribute.label
                                        }
                                    ) {
                                        Text(
                                            text = attribute.body,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF333333),
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        // Actions Row: BUY and CLOSE
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onBuy,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInCart) Color(0xFF2E7D32) else Color(0xFF1A1C1E),
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(28.dp),
                                enabled = buttonEnabled
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    buttonIcon?.let { icon ->
                                        Icon(icon, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text(
                                        buttonLabel,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                                        color = if (buttonEnabled) Color.White else Color.DarkGray
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                border = BorderStroke(1.dp, Color(0xFF1A1C1E).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1C1E))
                            ) {
                                Text(
                                    "CLOSE",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp)
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
private fun EditorialCard(
    label: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable { onToggle() },
        color = Color.White.copy(alpha = 0.95f),
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
