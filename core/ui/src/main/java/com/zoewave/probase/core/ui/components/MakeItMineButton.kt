package com.zoewave.probase.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ArchiveStatus

@Composable
fun MakeItMineButton(
    status: ArchiveStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF745E7A) // Default luxury purple
) {
    Button(
        onClick = onClick,
        enabled = status == ArchiveStatus.IDLE || status == ArchiveStatus.ERROR,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = if (status == ArchiveStatus.SUCCESS) Color(0xFF4CAF50) else Color.LightGray,
            contentColor = Color.White,
            disabledContentColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Crossfade(targetState = status, label = "Button State Animation") { currentState ->
            when (currentState) {
                ArchiveStatus.IDLE -> {
                    Text(
                        text = "MAKE IT MINE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                ArchiveStatus.ARCHIVING -> {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
                ArchiveStatus.SUCCESS -> {
                    Text(
                        text = "SAVED TO ARCHIVE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                ArchiveStatus.ERROR -> {
                    Text("ERROR - TRY AGAIN", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
