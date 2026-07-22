package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor

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
    // Dynamic Size: Grows from 18dp to 24dp when expanded
    val dotSize by animateDpAsState(
        targetValue = if (isExpanded) 24.dp else 18.dp,
        label = "dotSize"
    )

    val hasColor = !colorHex.isNullOrBlank()
    val baseColor = if (hasColor) parseColor(colorHex!!) else Color.White
    val dotBorder = if (hasColor) Color.White else Color.LightGray.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            // 1. Proportional Offset (Keeps the center pinned during animation)
            .offset(
                x = if (anchorAlignment == Alignment.TopEnd) (dotSize / 3) else -(dotSize / 3),
                y = -(dotSize / 3)
            )
            .size(dotSize)
            // 2. Outer Drop Shadow (Lifts the well off the Canvas line)
            .shadow(if (hasColor) 4.dp else 0.dp, CircleShape)
            .clip(CircleShape)
            // 3. The Base Pigment Fill
            .background(baseColor)
            // 4. The "Depth" Illusion (Dark inner shadow cascading from the top)
            .background(
                if (hasColor) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f), // Shadows the top edge
                            Color.Transparent // Fades to pure base color at the bottom
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            // 5. The Crisp Outer Ring
            .border(2.dp, dotBorder, CircleShape),
        contentAlignment = Alignment.BottomCenter // Aligns the inner highlight to the bottom
    ) {
        // 6. The "Meniscus / Liquid Bubble" Highlight
        if (hasColor) {
            Box(
                modifier = Modifier
                    .padding(bottom = 2.dp) // Pushes it slightly up from the white border
                    // Proportional sizing so the bubble scales naturally with the outer dot
                    .size(dotSize / 3.5f)
                    .clip(CircleShape)
                    // Semi-transparent white perfectly lightens whatever the base pigment is
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