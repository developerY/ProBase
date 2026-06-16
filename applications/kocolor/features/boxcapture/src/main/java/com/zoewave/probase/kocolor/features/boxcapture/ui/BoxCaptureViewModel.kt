package com.zoewave.probase.kocolor.features.boxcapture.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.features.boxcapture.data.LocalProductAnalyzer
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.BoxCaptureUiState
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureMode
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureStep
import com.zoewave.probase.core.model.ritual.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class ExtractedCosmetic(
    val name: String,
    val brand: String,
    val macroCategory: String? = null,
    val microCategory: String? = null,
    val formulation: String? = null,
    val chemistryBase: String? = null,
    val finish: String? = null,
    val coverage: String? = null,
    val shadeName: String? = null,
    val colorHex: String? = null,
    val instructions: String? = null,
    val batchCode: String? = null,
    val paoMonths: Int? = null,
    val price: Double? = null,
    val volume: String? = null
)

@HiltViewModel
class BoxCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiSettings: AiConfigurationSettings,
    private val repository: CosmeticInventoryRepository,
    private val localAnalyzer: LocalProductAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow<BoxCaptureUiState>(BoxCaptureUiState.Idle())
    val uiState: StateFlow<BoxCaptureUiState> = _uiState.asStateFlow()

    private val capturedUris = mutableListOf<String>()

    fun setMode(modeString: String) {
        val mode = try { CaptureMode.valueOf(modeString) } catch (e: Exception) { CaptureMode.BOX }
        _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), CaptureStep.getStepsForMode(mode).first(), mode)
    }

    fun onPhotoCaptured(uri: String) {
        capturedUris.add(uri)
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
        val nextStep = getNextStep(mode)
        if (nextStep != null) {
            _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), nextStep, mode)
        } else {
            analyzePhotos(mode)
        }
    }

    private fun getNextStep(mode: CaptureMode): CaptureStep? {
        val steps = CaptureStep.getStepsForMode(mode)
        val currentSize = capturedUris.size
        return steps.getOrNull(currentSize)
    }

    fun analyzePhotos(mode: CaptureMode) {
        viewModelScope.launch {
            val apiKey = aiSettings.getGeminiApiKey()
            
            if (apiKey.isNullOrBlank()) {
                runLocalAnalysis()
                return@launch
            }

            _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Querying Gemini Cloud...")
            
            try {
                val bitmaps = loadBitmaps()
                if (bitmaps.isEmpty()) {
                    _uiState.value = BoxCaptureUiState.Error("No valid images captured.")
                    return@launch
                }

                val model = GenerativeModel(
                    modelName = "gemini-1.5-pro",
                    apiKey = apiKey,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                    }
                )

                val prompt = content {
                    bitmaps.forEach { image(it) }
                    val target = if (mode == CaptureMode.BOX) "product box from all sides" else "product container (front and back)"
                    text("""
                        Analyze these photos of a $target.
                        Extract all available information, especially the ingredients list if visible on the back, and return it in the following JSON format:
                        {
                            "name": "Product Name",
                            "brand": "Brand Name",
                            "macroCategory": "COMPLEXION|EYES|LIPS|SKINCARE|FRAGRANCE|OTHER",
                            "microCategory": "FOUNDATION|CONCEALER|POWDER|EYESHADOW|LIPSTICK|SERUM|MOISTURIZER|CLEANSER|etc",
                            "formulation": "LIQUID|CREAM|POWDER|GEL|STICK|PENCIL|BALM|OIL|SPRAY|FOAM|LOOSE_POWDER|PRESSED_POWDER|UNKNOWN",
                            "chemistryBase": "WATER|SILICONE|OIL|ALCOHOL|MINERAL|WAX|HYBRID|UNKNOWN",
                            "finish": "MATTE|SATIN|NATURAL|DEWY|GLOSSY|SHIMMER|METALLIC|SHEER|VELVET|UNKNOWN",
                            "coverage": "SHEER|LIGHT|MEDIUM|FULL|BUILDABLE|NOT_APPLICABLE",
                            "shadeName": "Shade name if applicable",
                            "colorHex": "#RRGGBB if applicable",
                            "instructions": "Usage instructions",
                            "batchCode": "Batch code if found",
                            "paoMonths": 12,
                            "price": 42.0,
                            "volume": "30ml"
                        }
                        Return ONLY the JSON block. If a field is unknown, omit it or use null. Be precise.
                        If this is a product container without a box, prioritize the brand and product name from the front, and ingredients/volume from the back.
                    """.trimIndent())
                }

                val response = model.generateContent(prompt)
                val jsonText = response.text
                if (jsonText != null) {
                    val item = parseJsonToCosmeticItem(jsonText)
                    repository.saveCosmeticItem(item)
                    _uiState.value = BoxCaptureUiState.Success(item)
                } else {
                    _uiState.value = BoxCaptureUiState.Error("Failed to extract data from images.")
                }

            } catch (e: Exception) {
                _uiState.value = BoxCaptureUiState.Error("Analysis failed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun runLocalAnalysis() {
        _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Running Local AI (Offline)...")
        try {
            val bitmaps = loadBitmaps()
            if (bitmaps.isEmpty()) {
                _uiState.value = BoxCaptureUiState.Error("No valid images captured.")
                return
            }
            val item = localAnalyzer.analyze(bitmaps)
            repository.saveCosmeticItem(item)
            _uiState.value = BoxCaptureUiState.Success(item)
        } catch (e: Exception) {
            _uiState.value = BoxCaptureUiState.Error("Local analysis failed: ${e.localizedMessage}")
        }
    }

    private suspend fun loadBitmaps(): List<Bitmap> = withContext(Dispatchers.IO) {
        capturedUris.mapNotNull { uriString ->
            loadBitmapFromUri(Uri.parse(uriString))
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseJsonToCosmeticItem(jsonText: String): CosmeticItem {
        val json = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }
        return try {
            // Clean JSON string in case of LLM artifacts
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            
            val extracted = json.decodeFromString<ExtractedCosmetic>(finalJson)
            CosmeticItem(
                name = extracted.name,
                brand = extracted.brand,
                macroCategory = try { MacroCategory.valueOf(extracted.macroCategory ?: "TOOLS") } catch (e: Exception) { MacroCategory.TOOLS },
                microCategory = try { MicroCategory.valueOf(extracted.microCategory ?: "OTHER") } catch (e: Exception) { MicroCategory.OTHER },
                formulation = try { Formulation.valueOf(extracted.formulation ?: "UNKNOWN") } catch (e: Exception) { Formulation.UNKNOWN },
                chemistryBase = try { ChemistryBase.valueOf(extracted.chemistryBase ?: "UNKNOWN") } catch (e: Exception) { ChemistryBase.UNKNOWN },
                finish = try { Finish.valueOf(extracted.finish ?: "UNKNOWN") } catch (e: Exception) { Finish.UNKNOWN },
                coverage = try { Coverage.valueOf(extracted.coverage ?: "NOT_APPLICABLE") } catch (e: Exception) { Coverage.NOT_APPLICABLE },
                shadeName = extracted.shadeName,
                colorHex = extracted.colorHex,
                instructions = extracted.instructions,
                batchCode = extracted.batchCode,
                paoMonths = extracted.paoMonths,
                price = extracted.price,
                volume = extracted.volume,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            // Fallback for partial or malformed JSON
            CosmeticItem(
                name = "Extracted Product",
                brand = "Detected Brand",
                macroCategory = MacroCategory.COMPLEXION,
                microCategory = MicroCategory.FOUNDATION,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun reset() {
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
        capturedUris.clear()
        _uiState.value = BoxCaptureUiState.Idle(emptyList(), CaptureStep.getStepsForMode(mode).first(), mode)
    }
}
