package com.zoewave.probase.kocolor.mobile.features.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zoewave.probase.kocolor.mobile.features.home.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@Preview(showBackground = true)
@Composable
private fun QuickActionsPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuickActions(
                uiState = Unit,
                onEvent = {},
                navTo = {}
            )
        }
    }
}

data class QuickActionUiState(
    val title: String, 
    val subtitle: String, 
    val icon: ImageVector, 
    val color: Color, 
    val route: KoColorRoute
)

@Composable
fun QuickActions(
    uiState: Unit, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        QuickActionCard(
            uiState = QuickActionUiState(
                stringResource(R.string.applications_kocolor_apps_mobile_features_home_analyze_style), 
                stringResource(R.string.applications_kocolor_apps_mobile_features_home_ai_visual_analysis), 
                Icons.Default.AutoAwesome, 
                MaterialTheme.colorScheme.primary, 
                KoColorRoute.StyleSimulator
            ), 
            modifier = Modifier.weight(1f), 
            onEvent = {}, 
            navTo = navTo
        )
        /* Hide Capture Product for initial release
        QuickActionCard(
            uiState = QuickActionUiState(
                stringResource(R.string.applications_kocolor_apps_mobile_features_home_capture_product), 
                stringResource(R.string.applications_kocolor_apps_mobile_features_home_gemini_scanner), 
                Icons.Default.CameraAlt, 
                MaterialTheme.colorScheme.secondary, 
                KoColorRoute.Analyzer()
            ), 
            modifier = Modifier.weight(1f), 
            onEvent = {}, 
            navTo = navTo
        )
        */
    }
}

@Composable
fun QuickActionCard(
    uiState: QuickActionUiState, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    ElevatedCard(
        onClick = { navTo(uiState.route) }, 
        modifier = modifier, 
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(uiState.icon, null, tint = uiState.color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = uiState.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = uiState.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
