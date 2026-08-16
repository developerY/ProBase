package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.ui.util.PremiumProductImage
import com.zoewave.probase.core.ui.util.PremiumProductImage
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.core.ui.util.rememberBlurHashPainter
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

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (thumbnailUrl != null) {
                        PremiumProductImage(
                            imageUrl = thumbnailUrl,
                            blurHash = blurHash,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF5F5F5))
                                .border(8.dp, itemColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                            fallbackColor = itemColor
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                    Text(
                        text = notes?.editorialTitle ?: "Analyzing Product...",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF745E7A))
                    }
                } else if (notes != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        EditorialSection(label = "Usage Notes", content = notes.usageNotes)
                        EditorialSection(label = "Expert Tip", content = notes.expertTip)
                        notes.formulationInsight?.let { insight ->
                            EditorialSection(label = "Formulation Insight", content = insight)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("CLOSE", fontWeight = FontWeight.Black)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun EditorialSection(label: String, content: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )
    }
}
