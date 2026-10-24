package com.zoewave.probase.seaweed.mobile.review.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedQuad

@Composable
fun DocumentReviewScreen(
    rawSourcePurged: Boolean,
    highlightQuad: NormalizedQuad? = null
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (rawSourcePurged) {
                Text(
                    text = "Provenance Metadata Retained\n(Original scan & OCR text purged per 30-day privacy policy; source visual re-verification unavailable)",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Raw source image would be rendered here
                Text("Raw Document Image", color = Color.White.copy(alpha = 0.5f))

                // Overlay Canvas
                if (highlightQuad != null) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(highlightQuad.topLeft.x * size.width, highlightQuad.topLeft.y * size.height)
                            lineTo(highlightQuad.topRight.x * size.width, highlightQuad.topRight.y * size.height)
                            lineTo(highlightQuad.bottomRight.x * size.width, highlightQuad.bottomRight.y * size.height)
                            lineTo(highlightQuad.bottomLeft.x * size.width, highlightQuad.bottomLeft.y * size.height)
                            close()
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFFFF6F61), // Coral highlight
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                } else {
                    Text(
                        text = "Tap to inspect source highlight on scan",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                    )
                }
            }
        }

        // Draft Amount & Date Resolution Pane
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Interactive resolution prompt for NEEDS_USER_REVIEW_DRAFT
        }
    }
}
