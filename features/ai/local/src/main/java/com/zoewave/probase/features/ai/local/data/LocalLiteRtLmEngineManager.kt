package com.zoewave.probase.features.ai.local.data

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLiteRtLmEngineManager @Inject constructor(
    private val context: Context
) : ComponentCallbacks2 {

    private val engineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val engineScope = CoroutineScope(SupervisorJob() + engineDispatcher)

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private var isGenerating = false

    init {
        context.registerComponentCallbacks(this)
    }

    suspend fun initializeEngine(modelPath: String) = withContext(engineDispatcher) {
        if (engine != null) return@withContext

        try {
            val engineConfig = EngineConfig(modelPath)
            val newEngine = Engine(engineConfig)
            newEngine.initialize()
            engine = newEngine

            val samplerConfig = SamplerConfig(topK = 10, topP = 0.95, temperature = 0.8)
            val convConfig = ConversationConfig(samplerConfig = samplerConfig)
            conversation = newEngine.createConversation(convConfig)
        } catch (e: Exception) {
            e.printStackTrace()
            closeEngine()
        }
    }

    fun generateStream(prompt: String): Flow<String> = callbackFlow {
        isGenerating = true
        engineScope.launch {
            val activeConversation = conversation
            if (activeConversation == null) {
                close(IllegalStateException("Engine not initialized"))
                return@launch
            }
            try {
                activeConversation.sendMessageAsync(prompt).collect { message ->
                    trySend(message.toString())
                }
                isGenerating = false
                close()
            } catch (e: Exception) {
                isGenerating = false
                close(e)
            }
        }

        awaitClose {
            engineScope.launch {
                cancelCurrentGeneration()
            }
        }
    }.flowOn(engineDispatcher)

    private fun cancelCurrentGeneration() {
        if (isGenerating) {
            conversation?.cancelProcess()
            isGenerating = false
        }
    }

    fun closeEngine() {
        engineScope.launch {
            if (isGenerating) {
                cancelCurrentGeneration()
            }
            conversation?.close()
            conversation = null

            engine?.close()
            engine = null
        }
    }

    fun shutdown() {
        context.unregisterComponentCallbacks(this)
        closeEngine()
        engineScope.cancel()
        engineDispatcher.close()
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
            level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            closeEngine()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}
    override fun onLowMemory() {
        closeEngine()
    }
}
