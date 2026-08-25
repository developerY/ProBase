package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.ui.util.parseColor
import com.zoewave.probase.kocolor.features.analyzer.R
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.SimulatorEvent
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.shared.BlueprintDetailContent
import com.zoewave.probase.kocolor.model.KoColorRoute

@Composable
fun ResultStep(
    uiState: StyleSimulatorUiState,
    onEvent: (SimulatorEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    BlueprintDetailContent(
        title = stringResource(R.string.applications_kocolor_features_analyzer_simulator_blueprint),
        rationale = uiState.rationale,
        isLocalResult = uiState.isLocalResult,
        recommendedClothing = uiState.recommendedClothing,
        recommendedCosmetics = uiState.recommendedCosmetics,
        recommendedPalette = uiState.recommendedPalette,
        selectedResultTab = uiState.selectedResultTab,
        onTabSelected = { onEvent(SimulatorEvent.SelectResultTab(it)) },
        actionButtonText = stringResource(R.string.applications_kocolor_features_analyzer_simulator_lock_palette),
        onActionClick = { onEvent(SimulatorEvent.SaveToPalette) },
        navTo = navTo
    )
}
