package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.ritual.GlassButtonState

@Composable
fun GlassConnectionHeaderAction(
    buttonState: GlassButtonState,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (buttonState) {
        GlassButtonState.PROJECTING -> Color(0xFF4CAF50)
        GlassButtonState.READY_TO_START -> MaterialTheme.colorScheme.primary
        GlassButtonState.NO_GLASSES -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (buttonState) {
        GlassButtonState.PROJECTING -> Color.White
        GlassButtonState.READY_TO_START -> MaterialTheme.colorScheme.onPrimary
        GlassButtonState.NO_GLASSES -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    val icon = when (buttonState) {
        GlassButtonState.PROJECTING -> Icons.Default.CastConnected
        GlassButtonState.READY_TO_START -> Icons.Default.Cast
        GlassButtonState.NO_GLASSES -> Icons.Default.PhonelinkOff
    }

    Surface(
        onClick = onButtonClick,
        enabled = buttonState != GlassButtonState.NO_GLASSES,
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = "Project to Glass",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlassConnectionHeaderActionPreview() {
    MaterialTheme {
        GlassConnectionHeaderAction(buttonState = GlassButtonState.READY_TO_START, onButtonClick = {})
    }
}
