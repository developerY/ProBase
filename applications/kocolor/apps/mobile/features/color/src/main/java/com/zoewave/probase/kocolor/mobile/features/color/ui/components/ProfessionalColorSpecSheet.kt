package com.zoewave.probase.kocolor.mobile.features.color.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.colors.domain.model.ColorInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalColorSpecSheet(
    colorInfo: ColorInfo,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Professional Color Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Large Color Swatch
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(parseColor(colorInfo.hex))
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = colorInfo.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = colorInfo.hex.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // Spec Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SpecRow(label = "Pantone®", value = colorInfo.pantoneMatch?.let { "${it.code} (${it.name})" } ?: "None")
                colorInfo.cielab?.let { 
                    SpecRow(label = "CIELAB", value = "L: ${it.l.toInt()}, a: ${it.a.toInt()}, b: ${it.b.toInt()}") 
                }
                colorInfo.hsv?.let {
                    SpecRow(label = "HSV", value = "H: ${it.h.toInt()}°, S: ${(it.s * 100).toInt()}%, V: ${(it.v * 100).toInt()}%")
                }
                SpecRow(label = "Grade", value = if (colorInfo.isProfessionalGrade) "Professional" else "Standard")
            }
            
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}
