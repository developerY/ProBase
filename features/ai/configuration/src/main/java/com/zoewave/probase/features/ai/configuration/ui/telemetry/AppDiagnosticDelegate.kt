package com.zoewave.probase.features.ai.configuration.ui.telemetry

import com.zoewave.probase.features.ai.core.AiProvider

/**
 * Interface that allows individual apps (KoColor, Seaweed, etc.) to define
 * their own domain-specific AI diagnostic payload and validation steps,
 * while sharing the exact same AiDebugTelemetryScreen UI.
 */
interface AppDiagnosticDelegate {
    /** The initial list of diagnostic steps specific to this application */
    fun getInitialSteps(): List<DiagnosticStep>

    /**
     * Executes the live diagnostic test.
     * @param provider The AI Provider selected by the user in the UI.
     * @param updateStep Callback to update the state of a specific step in the UI.
     * @return The final raw string output payload to display in the UI.
     */
    suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): String?
}
