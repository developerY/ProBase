package com.zoewave.probase.kocolor.features.routines.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.features.routines.R
import com.zoewave.probase.core.model.ritual.RoutineStep
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun StepSummaryRow(
    uiState: RoutineStep, 
    onEvent: (Unit) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Surface(
        onClick = { onEvent(Unit) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.applications_kocolor_features_routines_ritual_stage_format, uiState.layeringOrder + 1), 
                    style = MaterialTheme.typography.labelSmall, 
                    modifier = Modifier.alpha(0.5f)
                )
            }
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.alpha(0.3f))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StepSummaryRowPreview() {
    MaterialTheme {
        StepSummaryRow(
            uiState = RoutineStep(id = "1", title = "Step 1", layeringOrder = 0), 
            onEvent = {},
            navTo = {}
        )
    }
}
