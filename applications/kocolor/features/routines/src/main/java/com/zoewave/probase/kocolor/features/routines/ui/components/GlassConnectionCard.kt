package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.UsbOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.ritual.GlassButtonState

@Composable
fun GlassConnectionCard(
    buttonState: GlassButtonState,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            data class StateVisuals(
                val text: String,
                val icon: ImageVector,
                val containerColor: Color,
                val contentColor: Color,
                val enabled: Boolean
            )

            val visuals = when (buttonState) {
                GlassButtonState.NO_GLASSES -> StateVisuals(
                    text = "Connect Glass Device",
                    icon = Icons.Default.UsbOff,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    enabled = false
                )
                GlassButtonState.READY_TO_START -> StateVisuals(
                    text = "Start Ritual Projection",
                    icon = Icons.Default.PermDeviceInformation,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    enabled = true
                )
                GlassButtonState.PROJECTING -> StateVisuals(
                    text = "Projecting Active",
                    icon = Icons.Default.CastConnected,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    enabled = true
                )
            }

            Button(
                onClick = onButtonClick,
                enabled = visuals.enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = visuals.containerColor,
                    contentColor = visuals.contentColor,
                    disabledContainerColor = visuals.containerColor,
                    disabledContentColor = visuals.contentColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(visuals.icon, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(visuals.text, fontWeight = FontWeight.Bold)
            }
        }
    }
}
