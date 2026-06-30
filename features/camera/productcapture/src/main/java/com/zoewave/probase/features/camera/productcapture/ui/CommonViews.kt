package com.zoewave.probase.features.camera.productcapture.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zoewave.probase.features.graphics.colorpicker.ui.ColorPickerDialog
import com.zoewave.probase.features.graphics.colorpicker.util.parseColor
import com.zoewave.probase.features.graphics.colorpicker.util.toHex

@Composable
fun AnalysisView(
    progress: String,
    themeColor: Color = Color(0xFF22d3ee)
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0f172a)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = themeColor)
        Spacer(modifier = Modifier.height(24.dp))
        Text(progress, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0f172a)).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Extraction Failed", color = Color.Red, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

data class ColorConfirmationUiState(
    val photoUri: String,
    val suggestedColors: List<String>,
    val selectedColorHex: String
)

@Composable
fun ColorConfirmationView(
    uiState: ColorConfirmationUiState,
    onColorSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onClear: () -> Unit,
    themeColor: Color = Color(0xFF22d3ee)
) {
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = parseColor(uiState.selectedColorHex),
            onColorSelected = { 
                onColorSelected(it.toHex()) 
                showColorPicker = false
            },
            onDismissRequest = { showColorPicker = false },
            title = "Refine Color"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0f172a))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI Color Analysis",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select the best shade from the photo",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
        ) {
            AsyncImage(
                model = uiState.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Surface(
                color = parseColor(uiState.selectedColorHex),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
                    .border(4.dp, Color.White, CircleShape),
                shadowElevation = 8.dp
            ) {}
        }

        Spacer(Modifier.height(32.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(uiState.suggestedColors) { _, hex ->
                val isSelected = hex == uiState.selectedColorHex
                Surface(
                    onClick = { onColorSelected(hex) },
                    color = parseColor(hex),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(50.dp)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {}
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f).height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("CLEAR")
            }

            IconButton(
                onClick = { showColorPicker = true },
                modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
            ) {
                Icon(Icons.Default.Palette, null, tint = themeColor)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor)
            ) {
                Text("USE COLOR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
