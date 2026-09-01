package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.zoewave.probase.kocolor.features.analyzer.R

/**
 * ML Kit Landmark point model.
 */
data class MlLandmark(
    val x: Float,
    val y: Float,
    val label: String = ""
)

/**
 * Result data payload containing image dimensions, bounding boxes, and landmark points.
 */
data class MlDetectionResult(
    val intrinsicImageWidth: Float,
    val intrinsicImageHeight: Float,
    val faceBoundingBox: Rect? = null,
    val hairBoundingBox: Rect? = null,
    val landmarks: List<MlLandmark> = emptyList(),
    val isFrontCamera: Boolean = true
)

/**
 * Production-ready ML Face Detection Findings Results Card with exact ContentScale.Crop ML Kit coordinate mapping.
 */
@Composable
fun FaceDetectionResultsCard(
    imageUri: String?,
    detectionResult: MlDetectionResult,
    seasonLabel: String = "WARM AUTUMN",
    temperature: String = "Warm",
    contrast: String = "Balanced",
    depth: String = "Medium",
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    var telemetryExpanded by remember { mutableStateOf(false) }
    var outputExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.applications_kocolor_features_analyzer_findings_title),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Image Overlay Box (ContentScale.Crop Math)
            CroppedFaceOverlayBox(
                imageUri = imageUri,
                detectionResult = detectionResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            // Established Season Text Section
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_established_season_format, seasonLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = stringResource(R.string.applications_kocolor_features_analyzer_findings_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

            // Expandable Accordions: ANALYSIS TELEMETRY
            AccordionRow(
                title = "ANALYSIS TELEMETRY",
                isExpanded = telemetryExpanded,
                onToggle = { telemetryExpanded = !telemetryExpanded }
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Format: RGBA_8888 (Native Bitmap mapping)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    Text("Engine: com.google.mlkit:face-detection (LANDMARK_MODE_ALL)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                    Text("Vectors: Skin (Cheek sampling), Iris (Eye bounding coords), Hair (Forehead projection)", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }

            // Expandable Accordions: OUTPUT ANALYSIS
            AccordionRow(
                title = "OUTPUT ANALYSIS",
                isExpanded = outputExpanded,
                onToggle = { outputExpanded = !outputExpanded }
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Aesthetic Profile List
                    Text(
                        text = "AESTHETIC PROFILE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        letterSpacing = 1.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BulletText("Temperature: $temperature")
                        BulletText("Contrast: $contrast")
                        BulletText("Depth: $depth")
                    }

                    Spacer(Modifier.height(4.dp))

                    // Color Swatch Grid (Two-Block Gradient Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Block: Summer (Cool/Light)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFE0F2FE), Color(0xFFFCE7F3))
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("Summer", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Text("Cool / Light", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }

                        // Right Block: Spring (Warm/Light)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFFFEF9C3), Color(0xFFFFEDD5))
                                    )
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Text("Spring", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Text("Warm / Light", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }

            // Action Button: CLOSE
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_features_analyzer_close),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED) // Accent Purple
                    )
                }
            }
        }
    }
}

/**
 * Cropped Face Overlay Box implementing the required ContentScale.Crop ML Kit coordinate mapping math:
 * scale = maxOf(size.width / intrinsicImageWidth, size.height / intrinsicImageHeight)
 * offsetX = (intrinsicImageWidth * scale - size.width) / 2f
 * offsetY = (intrinsicImageHeight * scale - size.height) / 2f
 * mappedX = (rawMLX * scale) - offsetX
 * mappedY = (rawMLY * scale) - offsetY
 */
@Composable
private fun CroppedFaceOverlayBox(
    imageUri: String?,
    detectionResult: MlDetectionResult,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRequest = remember(imageUri) {
        val model = if (!imageUri.isNullOrEmpty() && imageUri.startsWith("file://")) {
            Uri.parse(imageUri)
        } else {
            imageUri
        }
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .build()
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = "Analyzed Portrait",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val imgW = detectionResult.intrinsicImageWidth
            val imgH = detectionResult.intrinsicImageHeight
            if (imgW <= 0f || imgH <= 0f) return@Canvas

            // 1. Calculate Uniform Scale for ContentScale.Crop
            val scale = maxOf(size.width / imgW, size.height / imgH)

            // 2. Calculate Cropped Offsets
            val offsetX = (imgW * scale - size.width) / 2f
            val offsetY = (imgH * scale - size.height) / 2f

            // 3. Map Coordinates with Scale and Crop Offsets
            fun mapX(rawX: Float): Float {
                return (rawX * scale) - offsetX
            }

            fun mapY(rawY: Float): Float {
                return (rawY * scale) - offsetY
            }

            fun mapPoint(x: Float, y: Float): Offset {
                return Offset(mapX(x), mapY(y))
            }

            // Hair Bounding Box
            detectionResult.hairBoundingBox?.let { rect ->
                drawRect(
                    color = Color.Yellow.copy(alpha = 0.65f),
                    topLeft = Offset(mapX(rect.left.toFloat()), mapY(rect.top.toFloat())),
                    size = Size(rect.width() * scale, rect.height() * scale),
                    style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }

            // Face Bounding Box
            detectionResult.faceBoundingBox?.let { rect ->
                drawRoundRect(
                    color = Color.Cyan.copy(alpha = 0.5f),
                    topLeft = Offset(mapX(rect.left.toFloat()), mapY(rect.top.toFloat())),
                    size = Size(rect.width() * scale, rect.height() * scale),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )
            }

            // Landmark Nodes
            detectionResult.landmarks.forEach { landmark ->
                val center = mapPoint(landmark.x, landmark.y)
                drawCircle(
                    color = Color.Cyan,
                    radius = 12f,
                    center = center,
                    style = Stroke(width = 6f)
                )
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.35f),
                    radius = 24f,
                    center = center
                )
            }
        }
    }
}

@Composable
private fun AccordionRow(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = 1.sp
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            content()
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF7C3AED))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FaceDetectionResultsCardPreview() {
    val sampleResult = MlDetectionResult(
        intrinsicImageWidth = 720f,
        intrinsicImageHeight = 1280f,
        faceBoundingBox = Rect(160, 320, 560, 880),
        hairBoundingBox = Rect(160, 240, 560, 320),
        landmarks = listOf(
            MlLandmark(280f, 520f, "Left Cheek"),
            MlLandmark(440f, 520f, "Right Cheek"),
            MlLandmark(360f, 440f, "Left Eye")
        ),
        isFrontCamera = true
    )

    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            FaceDetectionResultsCard(
                imageUri = null,
                detectionResult = sampleResult,
                seasonLabel = "WARM AUTUMN"
            )
        }
    }
}
