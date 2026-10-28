package com.zoewave.probase.kocolor.mobile.features.color.ui.components

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.core.model.ritual.SavedAnalysis
import com.zoewave.probase.kocolor.mobile.features.color.R
import com.zoewave.probase.kocolor.model.KoColorRoute
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ColorHistoryCardUiState(val analysis: SavedAnalysis)

sealed class ColorHistoryEvent {
    data object Click : ColorHistoryEvent()
    data object Edit : ColorHistoryEvent()
    data object Copy : ColorHistoryEvent()
    data object Delete : ColorHistoryEvent()
}

@Composable
fun ColorHistoryCard(
    uiState: ColorHistoryCardUiState,
    modifier: Modifier = Modifier,
    onEvent: (ColorHistoryEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    val analysis = uiState.analysis
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()) }
    val dateStr = dateFormat.format(Date(analysis.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEvent(ColorHistoryEvent.Click) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = analysis.advice.seasonalType.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { onEvent(ColorHistoryEvent.Copy) }) {
                        Icon(Icons.Default.ContentCopy, stringResource(R.string.applications_kocolor_apps_mobile_features_color_copy), modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    IconButton(onClick = { onEvent(ColorHistoryEvent.Edit) }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.applications_kocolor_apps_mobile_features_color_edit), modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                    IconButton(onClick = { onEvent(ColorHistoryEvent.Delete) }) {
                        Icon(Icons.Default.DeleteOutline, stringResource(R.string.applications_kocolor_apps_mobile_features_color_delete), modifier = Modifier.size(18.dp), tint = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.weight(0.45f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    analysis.advice.faceUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    analysis.advice.clothesUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(0.55f)) {
                    Text(
                        text = analysis.advice.title ?: stringResource(R.string.applications_kocolor_apps_mobile_features_color_curated_look),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = analysis.advice.summary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF2C2420)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analysis.advice.recommendedPalette.take(5).forEach { hex ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(parseColor(hex))
                            .border(0.5.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}
