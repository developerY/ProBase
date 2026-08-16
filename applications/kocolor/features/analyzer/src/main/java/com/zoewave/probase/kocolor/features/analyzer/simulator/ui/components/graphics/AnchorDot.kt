package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

// Assuming this is your project's custom color parser
import com.zoewave.probase.core.ui.util.parseColor

/*
Why this creates the exact effect:
Layer 3 & 4 (The Base + Gradient): By stacking a Brush.verticalGradient directly on top of the solid
baseColor, we instantly turn a flat circle into a 3D recess. The top gets naturally shaded, and it bleeds
smoothly into the vibrant pigment at the bottom.

Layer G (The Bubble): By placing a child Box at Alignment.BottomCenter and filling it with
Color.White.copy(alpha = 0.55f), we don't have to calculate lighter hex codes for every pigment.
The alpha overlay automatically mixes with the base color underneath it to create a flawless, color-matched highlight.
 */

@Composable
fun AnchorDot(
    colorHex: String?,
    isExpanded: Boolean,
    anchorAlignment: Alignment,
    modifier: Modifier = Modifier
) {
    // 🛠️ 1. Dynamic Size: Bumps up to 32.dp for a distinct "selected" state
    val dotSize by animateDpAsState(
        targetValue = if (isExpanded) 32.dp else 18.dp,
        label = "dotSize"
    )

    // 🛠️ 2. Dynamic Glow: Animates a wide blur radius when expanded
    val glowRadius by animateDpAsState(
        targetValue = if (isExpanded) 16.dp else 0.dp,
        label = "glowRadius"
    )

    val hasColor = !colorHex.isNullOrBlank()
    val baseColor = if (hasColor) parseColor(colorHex!!) else Color.White
    val dotBorder = if (hasColor) Color.White else Color.LightGray.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            // Proportional Offset (Keeps the center pinned during animation)
            .offset(
                x = when (anchorAlignment) {
                    Alignment.TopEnd -> (dotSize / 3)
                    Alignment.TopStart -> -(dotSize / 3)
                    Alignment.BottomEnd -> (dotSize / 3)
                    Alignment.BottomStart -> -(dotSize / 3)
                    else -> 0.dp
                },
                y = if (anchorAlignment == Alignment.TopStart || anchorAlignment == Alignment.TopEnd || anchorAlignment == Alignment.TopCenter)
                    -(dotSize / 3) else (dotSize / 3)
            )
            .size(dotSize)
            // 🛠️ 3. The Glowing Halo Effect (MUST be placed before .clip!)
            .drawBehind {
                if (hasColor && glowRadius > 0.dp) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = baseColor.toArgb()
                            // Set alpha to ~60% opacity so it looks like radiant light, not solid paint
                            alpha = 150
                            maskFilter =
                                BlurMaskFilter(glowRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                        // Draw the glow slightly larger than the dot itself
                        canvas.nativeCanvas.drawCircle(
                            center.x,
                            center.y,
                            (size.width / 2f) + 2.dp.toPx(),
                            paint
                        )
                    }
                }
            }
            // Outer Drop Shadow (Lifts the well off the Canvas line)
            .shadow(if (hasColor) 4.dp else 0.dp, CircleShape)
            // The hard clip for the physical dot
            .clip(CircleShape)
            // The Base Pigment Fill
            .background(baseColor)
            // The "Depth" Illusion (Dark inner shadow cascading from the top)
            .background(
                if (hasColor) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            // The Crisp Outer Ring
            .border(2.dp, dotBorder, CircleShape),
        contentAlignment = Alignment.BottomCenter // Aligns the inner highlight to the bottom
    ) {
        // The "Meniscus / Liquid Bubble" Highlight
        if (hasColor) {
            Box(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    // Proportional sizing so the bubble scales naturally with the outer dot
                    .size(dotSize / 3.5f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.55f))
            )
        }
    }
}

/*
In Jetpack Compose, the order of modifiers strictly dictates the rendering layers. By moving .shadow()
to the top of the chain (before .background and .clip), I forced Compose to draw a standard, flat
drop shadow underneath the shape.

Your original code applied .shadow() at the very end of the chain. This forces Compose to render
he background and the white border first, and then apply the shadow graphics layer to the compiled result.
This creates a specific rendering quirk that casts dimensional depth, giving it that perfect inset
"liquid ink well" effect you created.
 */