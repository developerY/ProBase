package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PackPreviewBottomBar(
    selectedCount: Int,
    isLoading: Boolean,
    onImportSelected: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Button(
                onClick = onImportSelected,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedCount > 0 && !isLoading,
                shape = RoundedCornerShape(28.dp), // Pill shape like in mockup
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF745E7A).copy(alpha = 0.6f)) // Subdued luxury plum
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = "Import Selected ($selectedCount)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PackPreviewBottomBarPreview() {
    MaterialTheme {
        PackPreviewBottomBar(
            selectedCount = 4,
            isLoading = false,
            onImportSelected = {}
        )
    }
}
