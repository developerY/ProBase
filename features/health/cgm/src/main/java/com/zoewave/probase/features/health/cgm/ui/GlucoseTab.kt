package com.zoewave.probase.features.health.cgm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zoewave.probase.features.health.cgm.ui.components.CgmDashboard
import com.zoewave.probase.features.health.cgm.ui.components.CgmSelector

@Composable
fun GlucoseTab(
    modifier: Modifier = Modifier,
    viewModel: GlucoseViewModel = hiltViewModel()
) {
    val selectedSource by viewModel.selectedSource.collectAsState()
    val latestReading by viewModel.latestReading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CgmDashboard(
            reading = latestReading,
            onScanClick = { viewModel.triggerScan() },
            modifier = Modifier.padding(top = 16.dp)
        )

        CgmSelector(
            selectedSource = selectedSource,
            onSourceSelected = { viewModel.switchSource(it) },
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        )
    }
}
