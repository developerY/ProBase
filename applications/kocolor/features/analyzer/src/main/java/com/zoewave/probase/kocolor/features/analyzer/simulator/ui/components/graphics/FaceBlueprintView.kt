package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import android.graphics.BlurMaskFilter
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R

@Composable
fun FaceBlueprintView(
    data: VisualBlueprintData,
    modifier: Modifier = Modifier
) {
    // SINGLE SOURCE OF TRUTH: Tracks the currently expanded card
    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var lastTapCoords by remember { mutableStateOf<String?>(null) }

    // 1. Global Layout Shifts
    val blueprintOffset = 10.dp
    val horizontalShift = 15.dp

    // 2. Define Feature Anchor Points (Start of the lines)
    val eyeLeftAnchor = Offset(-52.dp.value, -54.dp.value)
    val eyeRightAnchor = Offset(46.dp.value, -54.dp.value)

    // 🛠️ Eyelid Path Points (Moved down from the brows to the lash line)
    val eyeLeftLidStart = Offset(-35.dp.value, -52.dp.value) // Inner corner
    val eyeLeftLidMid = Offset(-52.dp.value, -62.dp.value)   // Arch over pupil
    val eyeLeftLidEnd = Offset(-68.dp.value, -50.dp.value)   // Outer corner

    val eyeRightLidStart = Offset(28.dp.value, -52.dp.value) // Inner corner
    val eyeRightLidMid = Offset(46.dp.value, -62.dp.value)   // Arch over pupil
    val eyeRightLidEnd = Offset(62.dp.value, -50.dp.value)   // Outer corner

    // 🛠 ... Cheeks (Centered on the apples of the cheeks)
    val cheekRightAnchor = Offset(44.dp.value, 18.dp.value)

    // 🛠️ Lips (Perfectly centered and reshaped to the line art)
    val lipLeftCorner = Offset(-23.dp.value, 62.dp.value)
    val lipRightCorner = Offset(17.dp.value, 62.dp.value)
    val lipUpperAnchor = Offset(-3.dp.value, 53.dp.value)
    val lipLowerAnchor = Offset(-3.dp.value, 74.dp.value)

    // 3. Define Dynamic Callout Targets (End of the lines)
    // Eyes and Lips move TOWARD center when expanded so they remain fully visible.
    val eyesTarget by animateOffsetAsState(
        if (expandedCategory == "EYES") Offset(80f, -95f) else Offset(110f, -85f),
        label = "eyesTarget"
    )
    val cheeksTarget by animateOffsetAsState(
        if (expandedCategory == "CHEEKS") Offset(-90f, 130f) else Offset(-110f, 160f),
        label = "cheeksTarget"
    )
    val lipsTarget by animateOffsetAsState(
        if (expandedCategory == "LIPS") Offset(70f, 140f) else Offset(100f, 160f),
        label = "lipsTarget"
    )

    // 4. Animate Width
    val eyesWidth by animateDpAsState(if (expandedCategory == "EYES") 160.dp else 120.dp, label = "eyesWidth")
    val cheeksWidth by animateDpAsState(if (expandedCategory == "CHEEKS") 160.dp else 120.dp, label = "cheeksWidth")
    val lipsWidth by animateDpAsState(if (expandedCategory == "LIPS") 160.dp else 120.dp, label = "lipsWidth")

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Central Face Anchor
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = horizontalShift, y = blueprintOffset)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
        ) {
            Image(
                painter = painterResource(id = R.drawable.applications_kocolor_features_analyzer_face),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.4f),
                contentScale = ContentScale.Fit
            )
        }

        // Callout Lines & Shades
        val localDensity = LocalDensity.current
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // 🛠️ Shift-Aware Tap Detector
                        val centerX = size.width / 2f + horizontalShift.toPx()
                        val centerY = size.height / 2f + blueprintOffset.toPx()
                        with(localDensity) {
                            val dpX = (tapOffset.x - centerX).toDp().value.toInt()
                            val dpY = (tapOffset.y - centerY).toDp().value.toInt()

                            lastTapCoords = "X: $dpX, Y: $dpY"

                            Log.d("BlueprintCalibration", "--- FACE TAP DETECTED ---")
                            Log.d("BlueprintCalibration", "For Anchors: Offset(${dpX}.dp.value, ${dpY}.dp.value)")
                            Log.d("BlueprintCalibration", "For Targets: Offset(${dpX}f, ${dpY}f)")
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2 + horizontalShift.toPx(), size.height / 2 + blueprintOffset.toPx())

            // 1. Draw "Shades" (Soft Cosmetics - No Multiply)
            data.eyesItem?.colorHex?.let { hex ->
                // 🛠️ Bumped alpha to 0.4f so the eyeshadow pops
                val pigment = parseColor(hex).copy(alpha = 0.4f)

                fun drawBlurredCurve(start: Offset, control: Offset, end: Offset, thickness: Float = 14f) {
                    val path = Path().apply {
                        moveTo(center.x + start.x.dp.toPx(), center.y + start.y.dp.toPx())
                        quadraticTo(
                            center.x + control.x.dp.toPx(), center.y + control.y.dp.toPx(),
                            center.x + end.x.dp.toPx(), center.y + end.y.dp.toPx()
                        )
                    }

                    drawIntoCanvas { canvas ->
                        val paint = Paint().asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = pigment.toArgb()
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = thickness.dp.toPx()
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            maskFilter = BlurMaskFilter(15f.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                    }
                }

                // 🛠️ Using the new Eyelid coordinates
                drawBlurredCurve(eyeLeftLidStart, eyeLeftLidMid, eyeLeftLidEnd)
                drawBlurredCurve(eyeRightLidStart, eyeRightLidMid, eyeRightLidEnd)
            }

            data.cheeksItem?.colorHex?.let { hex ->
                // 🛠️ Bumped alpha to 0.45f to survive the massive blur
                val pigment = parseColor(hex).copy(alpha = 0.45f)

                fun drawCheekBlush(anchor: Offset) {
                    val cheekRect = android.graphics.RectF(
                        center.x + anchor.x.dp.toPx() - 35.dp.toPx(),
                        center.y + anchor.y.dp.toPx() - 15.dp.toPx(),
                        center.x + anchor.x.dp.toPx() + 35.dp.toPx(),
                        center.y + anchor.y.dp.toPx() + 15.dp.toPx()
                    )

                    drawIntoCanvas { canvas ->
                        val paint = Paint().asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = pigment.toArgb()
                            style = android.graphics.Paint.Style.FILL
                            maskFilter = BlurMaskFilter(35f.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                        canvas.nativeCanvas.drawOval(cheekRect, paint)
                    }
                }

                drawCheekBlush(cheekRightAnchor)
            }

            data.lipsItem?.colorHex?.let { hex ->
                // 🛠️ Bumped alpha to 0.65f for rich, full lip coverage
                val pigment = parseColor(hex).copy(alpha = 0.65f)

                val lipPath = Path().apply {
                    moveTo(center.x + lipLeftCorner.x.dp.toPx(), center.y + lipLeftCorner.y.dp.toPx())
                    quadraticTo(
                        center.x + lipUpperAnchor.x.dp.toPx(), center.y + lipUpperAnchor.y.dp.toPx(),
                        center.x + lipRightCorner.x.dp.toPx(), center.y + lipRightCorner.y.dp.toPx()
                    )
                    quadraticTo(
                        center.x + lipLowerAnchor.x.dp.toPx(), center.y + lipLowerAnchor.y.dp.toPx(),
                        center.x + lipLeftCorner.x.dp.toPx(), center.y + lipLeftCorner.y.dp.toPx()
                    )
                    close()
                }

                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        color = pigment.toArgb()
                        style = android.graphics.Paint.Style.FILL
                        maskFilter = BlurMaskFilter(6f.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawPath(lipPath.asAndroidPath(), paint)
                }
            }

            // 2. Draw Callout Lines (Using animated targets)
            val lineStroke = 0.8.dp.toPx()
            val anchorRadius = 2.dp.toPx()
            val lineColor = Color.DarkGray.copy(alpha = 0.4f)

            // EYES Line (Attaches to Right Eye)
            drawLine(lineColor, Offset(center.x + eyeRightAnchor.x.dp.toPx(), center.y + eyeRightAnchor.y.dp.toPx()), Offset(center.x + eyesTarget.x.dp.toPx(), center.y + eyesTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + eyeRightAnchor.x.dp.toPx(), center.y + eyeRightAnchor.y.dp.toPx()))

            // CHEEKS Line (Attaches to Left Cheek)
            drawLine(lineColor, Offset(center.x + cheekRightAnchor.x.dp.toPx(), center.y + cheekRightAnchor.y.dp.toPx()), Offset(center.x + cheeksTarget.x.dp.toPx(), center.y + cheeksTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + cheekRightAnchor.x.dp.toPx(), center.y + cheekRightAnchor.y.dp.toPx()))

            // LIPS Line (Attaches to Lower Lip)
            drawLine(lineColor, Offset(center.x + lipLowerAnchor.x.dp.toPx(), center.y + lipLowerAnchor.y.dp.toPx()), Offset(center.x + lipsTarget.x.dp.toPx(), center.y + lipsTarget.y.dp.toPx()), lineStroke)
            drawCircle(lineColor, anchorRadius, Offset(center.x + lipLowerAnchor.x.dp.toPx(), center.y + lipLowerAnchor.y.dp.toPx()))
        }

        // 5. Render the Callouts
        val calloutHalfHeight = 24.dp // Pushes the card down so the top-corner dot hits the line

        // --- EYES CALLOUT (Right Side) ---
        BlueprintCallout(
            label = "EYES",
            productName = data.eyesItem?.name ?: "Pending...",
            colorHex = data.eyesItem?.colorHex,
            isExpanded = expandedCategory == "EYES",
            onExpandToggle = { expandedCategory = if (expandedCategory == "EYES") null else "EYES" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "EYES") 10f else 1f)
                .width(eyesWidth)
                .offset(
                    // Add half-width to pin the TopStart (Left) corner dot to the line
                    x = horizontalShift + eyesTarget.x.dp + (eyesWidth / 2),
                    y = blueprintOffset + eyesTarget.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopStart
        )

        // --- CHEEKS CALLOUT (Left Side) ---
        BlueprintCallout(
            label = "CHEEKS",
            productName = data.cheeksItem?.name ?: "Pending...",
            colorHex = data.cheeksItem?.colorHex,
            isExpanded = expandedCategory == "CHEEKS",
            onExpandToggle = { expandedCategory = if (expandedCategory == "CHEEKS") null else "CHEEKS" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "CHEEKS") 10f else 1f)
                .width(cheeksWidth)
                .offset(
                    // Subtract half-width to pin the TopEnd (Right) corner dot to the line
                    x = horizontalShift + cheeksTarget.x.dp - (cheeksWidth / 2),
                    y = blueprintOffset + cheeksTarget.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopEnd
        )

        // --- LIPS CALLOUT (Right Side) ---
        BlueprintCallout(
            label = "LIPS",
            productName = data.lipsItem?.name ?: "Pending...",
            colorHex = data.lipsItem?.colorHex,
            isExpanded = expandedCategory == "LIPS",
            onExpandToggle = { expandedCategory = if (expandedCategory == "LIPS") null else "LIPS" },
            modifier = Modifier
                .zIndex(if (expandedCategory == "LIPS") 10f else 1f)
                .width(lipsWidth)
                .offset(
                    // Add half-width to pin the TopStart (Left) corner dot to the line
                    x = horizontalShift + lipsTarget.x.dp + (lipsWidth / 2),
                    y = blueprintOffset + lipsTarget.y.dp + calloutHalfHeight
                ),
            anchorAlignment = Alignment.TopStart
        )

        // Live Coordinate Overlay (Visible in Interactive Preview)
        lastTapCoords?.let { coords ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = coords,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FaceBlueprintViewPreview() {
    FaceBlueprintView(data = VisualBlueprintData())
}