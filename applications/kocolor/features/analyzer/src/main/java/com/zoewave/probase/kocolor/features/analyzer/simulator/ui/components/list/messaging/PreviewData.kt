package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

import android.graphics.PointF
import android.graphics.Rect
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.FaceTelemetryData
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.StyleSimulatorUiState

internal object MessagingPreviewData {
    val sampleTelemetry = FaceTelemetryData(
        imageWidth = 1000,
        imageHeight = 1000,
        cheekPoint = PointF(500f, 600f),
        eyePoint = PointF(400f, 400f),
        hairBoundingBox = Rect(300, 100, 700, 250),
        faceBoundingBox = Rect(200, 300, 800, 900),
        skinLuminance = 0.6543f,
        eyeLuminance = 0.2121f,
        hairLuminance = 0.1515f,
        contrastDelta = 0.5028f,
        undertoneScore = 0.2235f
    )

    val sampleUiState = StyleSimulatorUiState(
        userPortraitUri = "https://example.com/portrait.jpg",
        fashionProfileLabel = "NEUTRAL SPRING",
        faceTelemetry = sampleTelemetry
    )
}
