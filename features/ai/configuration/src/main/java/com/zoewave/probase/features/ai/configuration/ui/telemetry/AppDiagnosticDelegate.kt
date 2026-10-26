package com.zoewave.probase.features.ai.configuration.ui.telemetry

import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

data class DiagnosticRunReport(
    val passed: Boolean,
    val providerName: String,
    val latencyMs: Long,
    val rawResponseJson: String?,
    val failureReason: String? = null
)

interface AppDiagnosticDelegate {
    val appName: String
    fun getInitialSteps(): List<DiagnosticStep>

    suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): DiagnosticRunReport
}

@Singleton
class DefaultPingDiagnosticDelegate @Inject constructor() : AppDiagnosticDelegate {
    override val appName: String = "Generic Probase App"

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Checking provider availability"),
        DiagnosticStep(2, "Command & Prompt", "Sending synthetic JSON ping"),
        DiagnosticStep(3, "Response & Latency", "Awaiting model output"),
        DiagnosticStep(4, "Answer & Schema", "Verifying status JSON")
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (Int, DiagnosticStepState, String) -> Unit
    ): DiagnosticRunReport {
        val startMs = System.currentTimeMillis()
        updateStep(2, DiagnosticStepState.RUNNING, "Dispatching ping prompt...")
        val prompt = """Respond ONLY in valid JSON: {"status":"ok","engine":"active"}"""
        
        return runCatching {
            updateStep(2, DiagnosticStepState.SUCCESS, "Prompt dispatched (${prompt.length} chars)")
            updateStep(3, DiagnosticStepState.RUNNING, "Waiting for ${provider.capability.displayName}...")
            
            val result = provider.execute(AiInput.TextOnly(prompt))
            val latency = System.currentTimeMillis() - startMs
            
            if (result.isSuccess) {
                val raw = result.getOrNull() ?: ""
                updateStep(3, DiagnosticStepState.SUCCESS, "Responded in ${latency}ms")

                val valid = raw.contains("\"status\"") && raw.contains("\"ok\"")
                if (valid) {
                    updateStep(4, DiagnosticStepState.SUCCESS, "Valid JSON ping confirmed")
                    DiagnosticRunReport(true, provider.capability.displayName, latency, raw)
                } else {
                    updateStep(4, DiagnosticStepState.FAILED, "Unexpected payload structure")
                    DiagnosticRunReport(false, provider.capability.displayName, latency, raw, "Invalid ping JSON")
                }
            } else {
                val err = result.exceptionOrNull()
                updateStep(3, DiagnosticStepState.FAILED, err?.message ?: "Execution failed")
                updateStep(4, DiagnosticStepState.FAILED, "Skipped due to execution error")
                DiagnosticRunReport(false, provider.capability.displayName, latency, null, err?.message)
            }
        }.getOrElse { err ->
            if (err is kotlinx.coroutines.CancellationException) throw err
            val latency = System.currentTimeMillis() - startMs
            updateStep(3, DiagnosticStepState.FAILED, err.message ?: "Execution failed")
            updateStep(4, DiagnosticStepState.FAILED, "Skipped due to execution error")
            DiagnosticRunReport(false, provider.capability.displayName, latency, null, err.message)
        }
    }
}

// Note: We use a wrapper class for the resolved delegate to avoid circular dependency / duplicate binding issues
// with Hilt when an app explicitly binds AppDiagnosticDelegate.
class ResolvedDiagnosticDelegate(
    val delegate: AppDiagnosticDelegate
)

@Module
@InstallIn(SingletonComponent::class)
abstract class DiagnosticOptionalModule {
    @BindsOptionalOf
    abstract fun bindOptionalAppDiagnosticDelegate(): AppDiagnosticDelegate

    companion object {
        @Provides
        @Singleton
        fun provideResolvedDiagnosticDelegate(
            customDelegate: Optional<AppDiagnosticDelegate>,
            defaultDelegate: DefaultPingDiagnosticDelegate
        ): ResolvedDiagnosticDelegate {
            return ResolvedDiagnosticDelegate(customDelegate.orElse(defaultDelegate))
        }
    }
}
