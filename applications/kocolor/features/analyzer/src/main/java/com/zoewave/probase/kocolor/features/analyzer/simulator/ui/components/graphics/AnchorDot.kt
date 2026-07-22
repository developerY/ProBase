package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor

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

    // Dynamic Styling: Hollow if missing, filled if present
    val hasColor = !colorHex.isNullOrBlank()
    val dotBackground = if (hasColor) parseColor(colorHex!!) else Color.White
    val dotBorder = if (hasColor) Color.White else Color.LightGray.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .offset(
                x = if (anchorAlignment == Alignment.TopEnd) (dotSize / 3) else -(dotSize / 3),
                y = -(dotSize / 3)
            )
            .size(dotSize)
            // RESTORED INK WELL ORDER: Clip -> Background -> Border -> Shadow
            .clip(CircleShape)
            .background(dotBackground)
            .border(2.dp, dotBorder, CircleShape)
            .shadow(if (hasColor) 4.dp else 0.dp, CircleShape)
    )
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