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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val route: KoColorRoute,
    val isExperimental: Boolean = false
)

@Composable
fun QuickActions(
    uiState: Unit, 
    modifier: Modifier = Modifier,
    onEvent: (Unit) -> Unit, 
    navTo: (KoColorRoute) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickActionCard(
                uiState = QuickActionUiState(
                    "Calibration",
                    "Scan your color profile",
                    Icons.Default.AutoAwesome,
                    MaterialTheme.colorScheme.primary,
                    KoColorRoute.Calibration,
                    isExperimental = true
                ),
                modifier = Modifier.weight(1f),
                onEvent = {},
                navTo = navTo
            )
            QuickActionCard(
                uiState = QuickActionUiState(
                    "Style Playlist",
                    "7-day style forecast",
                    Icons.Default.AutoAwesome,
                    MaterialTheme.colorScheme.secondary,
                    KoColorRoute.StylePlaylist,
                    isExperimental = true
                ),
                modifier = Modifier.weight(1f),
                onEvent = {},
                navTo = navTo
            )
        }
        
        QuickActionCard(
            uiState = QuickActionUiState(
                stringResource(R.string.applications_kocolor_apps_mobile_features_home_analyze_style), 
                stringResource(R.string.applications_kocolor_apps_mobile_features_home_ai_visual_analysis), 
                Icons.Default.AutoAwesome, 
                MaterialTheme.colorScheme.tertiary, 
                KoColorRoute.StyleSimulator,
                isExperimental = true
            ), 
            modifier = Modifier.fillMaxWidth(), 
            onEvent = {}, 
            navTo = navTo
        )
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
        modifier = modifier.height(100.dp), 
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = uiState.icon, 
                contentDescription = null, 
                tint = uiState.color, 
                modifier = Modifier.size(36.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = uiState.subtitle, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.isExperimental) {
                Surface(
                    color = Color(0xFF6750A4),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.applications_kocolor_apps_mobile_features_home_experimental),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
