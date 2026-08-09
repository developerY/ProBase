package com.zoewave.probase.kocolor.features.starterpack.ui.packpreview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PackPreviewBottomBar(
    selectedCount: Int,
    isLoading: Boolean,
    onImportSelected: () -> Unit,
    onWipe: () -> Unit,
    isWipeVisible: Boolean = false
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onImportSelected,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = selectedCount > 0 && !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF745E7A).copy(alpha = 0.8f))
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

            if (isWipeVisible) {
                Button(
                    onClick = onWipe,
                    modifier = Modifier
                        .size(height = 56.dp, width = 64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Wipe Collection",
                        modifier = Modifier.size(24.dp)
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
            onImportSelected = {},
            onWipe = {},
            isWipeVisible = true
        )
    }
}
