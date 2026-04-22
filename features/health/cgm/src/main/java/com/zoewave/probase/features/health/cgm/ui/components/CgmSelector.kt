package com.zoewave.probase.features.health.cgm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zoewave.probase.core.model.health.GlucoseSource

@Composable
fun CgmSelector(
    selectedSource: GlucoseSource,
    onSourceSelected: (GlucoseSource) -> Unit,
    modifier: Modifier = Modifier
) {
    val manufacturers = listOf(
        ManufacturerInfo(GlucoseSource.DEXCOM_SHARE, "Dexcom", "High"),
        ManufacturerInfo(GlucoseSource.LIFESCAN_ONETOUCH, "LifeScan OneTouch", "High"),
        ManufacturerInfo(GlucoseSource.ABBOTT_LIBRE_LINK_UP, "Abbott LibreLinkUp", "Medium"),
        ManufacturerInfo(GlucoseSource.MEDTRONIC_CARELINK, "Medtronic CareLink", "Medium"),
        ManufacturerInfo(GlucoseSource.ASCENSIA_CONTOUR, "Ascensia Contour", "Medium"),
        ManufacturerInfo(GlucoseSource.MEDTRUM, "Medtrum", "Medium"),
        ManufacturerInfo(GlucoseSource.SIBIONICS, "SiBionics", "Medium"),
        ManufacturerInfo(GlucoseSource.TRIVIDIA_TRUE_METRIX, "Trividia TRUE Metrix", "Medium"),
        ManufacturerInfo(GlucoseSource.BLE_STANDARD, "Standard Bluetooth", "Low"),
        ManufacturerInfo(GlucoseSource.LIBRE_NFC, "Libre NFC Scan", "Low"),
        ManufacturerInfo(GlucoseSource.SIMULATOR, "Simulator", "None")
    )

    Column(modifier = modifier) {
        Text(
            text = "Select Glucose Source",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            manufacturers.forEach { info ->
                ManufacturerItem(
                    info = info,
                    isSelected = selectedSource == info.source,
                    onClick = { onSourceSelected(info.source) }
                )
            }
        }
    }
}

@Composable
private fun ManufacturerItem(
    info: ManufacturerInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = info.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "API Accessibility: ${info.accessibility}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (info.accessibility == "High") Color(0xFF4CAF50) else Color.Gray
                )
            }
            if (isSelected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Selected")
            }
        }
    }
}

private data class ManufacturerInfo(
    val source: GlucoseSource,
    val name: String,
    val accessibility: String
)
