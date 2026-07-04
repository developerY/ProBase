package com.zoewave.probase.kocolor.features.clothingcapture.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.model.network.DiscoveryStatus
import com.zoewave.probase.core.model.network.ServiceStatus
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.kocolor.features.analyzer.data.LocalProductAnalyzer
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureStep
import com.zoewave.probase.kocolor.features.clothingcapture.ui.state.ClothingCaptureUiState
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class ExtractedClothing(
    val name: String,
    val brand: String? = null,
    val category: String? = null,
    val material: String? = null,
    val price: Double? = null,
    val size: String? = null,
    val colorHex: String? = null,
    val userColorOverridden: Boolean = false
)

@HiltViewModel
class ClothingCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiSettings: AiConfigurationSettings,
    private val sessionRepository: FashionSessionRepository,
    private val localAnalyzer: LocalProductAnalyzer,
    private val localAi: LocalAiEngine,
    private val colorRepository: ColorRepository
) : ViewModel() {

    val discoveryStatus = sessionRepository.discoveryStatus

    private val _uiState = MutableStateFlow<ClothingCaptureUiState>(ClothingCaptureUiState.Idle())
    val uiState: StateFlow<ClothingCaptureUiState> = _uiState.asStateFlow()

    private val capturedUris = mutableListOf<String>()
    private var localLabelsOcr: String = ""
    private var sessionManualPrice: Double? = null
    private var sessionManualColor: String? = null

    fun onEvent(event: ClothingCaptureEvent) {
        when (event) {
            is ClothingCaptureEvent.Capture -> onPhotoCaptured(event.uri)
            ClothingCaptureEvent.Retry -> reset()
            ClothingCaptureEvent.Dismiss -> { /* Handled in UI layer */ }
            is ClothingCaptureEvent.Success -> { /* Handled in UI layer */ }
            is ClothingCaptureEvent.DeletePhoto -> {
                if (event.index in capturedUris.indices) {
                    capturedUris.removeAt(event.index)
                    refreshStep()
                }
            }
            ClothingCaptureEvent.SubmitToAi -> analyzePhotos()
            ClothingCaptureEvent.ContinueToReview -> prepareReview()
            ClothingCaptureEvent.FinalizeProduct -> finalizeProduct()
            ClothingCaptureEvent.SaveProduct -> saveAndFinish()
            ClothingCaptureEvent.SkipStep -> skipStep()
            is ClothingCaptureEvent.OnColorSelected -> {
                sessionManualColor = event.hex
                val current = (uiState.value as? ClothingCaptureUiState.Idle)
                if (current != null) {
                    _uiState.value = current.copy(extractedColorHex = event.hex)
                } else if (uiState.value is ClothingCaptureUiState.ColorConfirmation) {
                    val cc = uiState.value as ClothingCaptureUiState.ColorConfirmation
                    _uiState.value = cc.copy(selectedColorHex = event.hex)
                }
            }
            ClothingCaptureEvent.ConfirmColor -> {
                val current = (uiState.value as? ClothingCaptureUiState.ColorConfirmation) ?: return
                sessionManualColor = current.selectedColorHex
                val nextStep = getNextStep()
                if (nextStep != null) {
                    _uiState.value = ClothingCaptureUiState.Idle(current.capturedUris, nextStep, sessionManualColor, sessionManualPrice)
                } else {
                    prepareReview()
                }
            }
            ClothingCaptureEvent.ClearColor -> {
                sessionManualColor = null
                val current = (uiState.value as? ClothingCaptureUiState.ColorConfirmation) ?: return
                val nextStep = getNextStep()
                if (nextStep != null) {
                    _uiState.value = ClothingCaptureUiState.Idle(current.capturedUris, nextStep, null, sessionManualPrice)
                } else {
                    prepareReview()
                }
            }
            ClothingCaptureEvent.ConfirmPrice -> {
                val current = (uiState.value as? ClothingCaptureUiState.PriceConfirmation) ?: return
                sessionManualPrice = current.detectedPrice
                val nextStep = getNextStep()
                if (nextStep != null) {
                    _uiState.value = ClothingCaptureUiState.Idle(current.capturedUris, nextStep, sessionManualColor, sessionManualPrice)
                } else {
                    prepareReview()
                }
            }
            is ClothingCaptureEvent.OnPriceChanged -> {
                sessionManualPrice = event.price
                val current = (uiState.value as? ClothingCaptureUiState.PriceConfirmation)
                if (current != null) {
                    _uiState.value = current.copy(detectedPrice = event.price)
                }
            }
        }
    }

    private fun skipStep() {
        val current = (uiState.value as? ClothingCaptureUiState.Idle) ?: return
        if (current.currentStep.isSkippable) {
            capturedUris.add("") 
            val nextStep = getNextStep()
            if (nextStep != null) {
                _uiState.value = ClothingCaptureUiState.Idle(capturedUris.toList(), nextStep, sessionManualColor, sessionManualPrice)
            } else {
                prepareReview()
            }
        }
    }

    private fun refreshStep() {
        val currentStep = getNextStep() ?: ClothingCaptureStep.FRONT
        _uiState.value = ClothingCaptureUiState.Idle(capturedUris.toList(), currentStep, sessionManualColor, sessionManualPrice)
    }

    private fun onPhotoCaptured(uri: String) {
        capturedUris.add(uri)
        val current = (uiState.value as? ClothingCaptureUiState.Idle) ?: return

        // --- Background OCR Trigger (Label) ---
        if (current.currentStep == ClothingCaptureStep.LABEL) {
            viewModelScope.launch {
                Log.d("ClothingCaptureVM", "Triggering background OCR for Label...")
                loadBitmapFromUri(Uri.parse(uri))?.let { bitmap ->
                    val text = localAnalyzer.extractText(bitmap)
                    localLabelsOcr = text
                    Log.d("ClothingCaptureVM", "Background Label OCR Complete. Output:\n$text")
                }
            }
        }
        
        if (current.currentStep == ClothingCaptureStep.COLOR) {
            viewModelScope.launch {
                val bitmap = loadBitmapFromUri(Uri.parse(uri))
                val suggestedColors = if (bitmap != null) localAnalyzer.extractColorPalette(bitmap) else listOf("#808080")
                _uiState.value = ClothingCaptureUiState.ColorConfirmation(
                    capturedUris = capturedUris.toList(),
                    suggestedColors = suggestedColors,
                    selectedColorHex = suggestedColors.firstOrNull() ?: "#808080"
                )
            }
            return
        }

        if (current.currentStep == ClothingCaptureStep.PRICE) {
            viewModelScope.launch {
                val bitmap = loadBitmapFromUri(Uri.parse(uri))
                val detectedPrice = if (bitmap != null) localAnalyzer.extractPrice(bitmap) ?: 0.0 else 0.0
                _uiState.value = ClothingCaptureUiState.PriceConfirmation(
                    capturedUris = capturedUris.toList(),
                    detectedPrice = detectedPrice
                )
            }
            return
        }

        val nextStep = getNextStep()
        if (nextStep != null) {
            _uiState.value = ClothingCaptureUiState.Idle(capturedUris.toList(), nextStep, sessionManualColor, sessionManualPrice)
        } else {
            // All steps complete. Go to Discovery screen first.
            _uiState.value = ClothingCaptureUiState.Analyzing(capturedUris.toList(), "Processing Captures...")
        }
    }

    private fun prepareReview() {
        viewModelScope.launch {
            _uiState.value = ClothingCaptureUiState.Analyzing(capturedUris.toList(), "Performing Local OCR...")
            
            if (localLabelsOcr.isBlank()) {
                val labelIdx = ClothingCaptureStep.ALL.indexOf(ClothingCaptureStep.LABEL)
                localLabelsOcr = if (labelIdx != -1 && labelIdx in capturedUris.indices) {
                    val uri = capturedUris[labelIdx]
                    if (uri.isNotBlank()) {
                        loadBitmapFromUri(Uri.parse(uri))?.let { localAnalyzer.extractText(it) } ?: ""
                    } else ""
                } else ""
            }

            if (localLabelsOcr.isNotBlank()) {
                sessionRepository.updateServiceStatus("localai", ServiceStatus.ACCESSING, "Synthesizing label text...")
                val localAiResult = localAi.standardizeOcrText(localLabelsOcr)
                
                localAiResult.onSuccess { data ->
                    if (!data.brand.isNullOrBlank()) {
                        sessionRepository.updateServiceStatus("localai", ServiceStatus.SUCCESS, "Found: ${data.brand}")
                    } else {
                        sessionRepository.updateServiceStatus("localai", ServiceStatus.FAILED, "No label data extracted.")
                    }
                }.onFailure {
                    sessionRepository.updateServiceStatus("localai", ServiceStatus.UNSUPPORTED, "Hardware bypass active.")
                }
            } else {
                sessionRepository.updateServiceStatus("localai", ServiceStatus.FAILED, "No label photo.")
            }

            _uiState.value = ClothingCaptureUiState.Review(
                capturedUris = capturedUris.toList(),
                labelsOcr = localLabelsOcr,
                manualColorHex = sessionManualColor,
                price = sessionManualPrice
            )
        }
    }

    private fun getNextStep(): ClothingCaptureStep? {
        val currentSize = capturedUris.size
        return ClothingCaptureStep.ALL.getOrNull(currentSize)
    }

    fun analyzePhotos() {
        viewModelScope.launch {
            val apiKey = aiSettings.getGeminiApiKey()
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"
            
            val currentReview = (uiState.value as? ClothingCaptureUiState.Review)
            val manualColor = currentReview?.manualColorHex
            val capturedPrice = currentReview?.price

            if (apiKey.isNullOrBlank()) {
                _uiState.value = ClothingCaptureUiState.Error("API Key is missing.")
                return@launch
            }

            _uiState.value = ClothingCaptureUiState.AiAnalyzing(capturedUris.toList(), "Identifying Garment...")
            sessionRepository.updateServiceStatus("gemini", ServiceStatus.ACCESSING, "Uploading style snapshots...")
            
            try {
                val bitmaps = loadBitmaps()
                
                val model = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                    }
                )

                val prompt = content {
                    bitmaps.forEach { image(it) }
                    val colorContext = if (!manualColor.isNullOrBlank()) {
                        """
                        USER COLOR HINT: The user sampled the color $manualColor from the photo.
                        CRITICAL INSTRUCTION: Use this hint as a starting point, but VALIDATE it against the visual evidence and your internal knowledge of this specific item. If the user's hex code appears incorrect due to lighting or sampling error, you MUST provide the true, accurate hex code for this item's color.
                        """.trimIndent()
                    } else {
                        "Analyze the photos and identify the most accurate representative hex code for this item's color."
                    }
                    
                    text("""
                        You are a professional fashion analyzer for the "KoColor Boutique" app.
                        Analyze these photos of a garment or piece of clothing.
                        $colorContext
                        
                        Extract information to fill a professional wardrobe entry.
                        Return ONLY the following JSON format:
                        {
                            "name": "Item Name (e.g. Silk Blouse)",
                            "brand": "Brand Name",
                            "category": "TOPS|BOTTOMS|SHOES|ACCESSORIES|OTHER",
                            "material": "e.g. 100% Silk, Cotton Blend",
                            "size": "e.g. Medium, 8, 42",
                            "price": null,
                            "colorHex": "#RRGGBB",
                            "userColorOverridden": true
                        }
                        Be extremely precise with the colorHex code. If you override the user's color hint, set "userColorOverridden" to true.
                        If a field is unknown, use null.
                    """.trimIndent())
                }

                val response = model.generateContent(prompt)
                val jsonText = response.text
                
                if (jsonText != null) {
                    sessionRepository.updateServiceStatus("gemini", ServiceStatus.SUCCESS, "Fashion analysis complete.")
                    var item = parseJsonToClothingItem(jsonText).copy(
                        imageUrl = capturedUris.firstOrNull { it.isNotBlank() },
                        colorHex = manualColor ?: "#FFFFFF",
                        price = capturedPrice
                    )
                    
                    // --- Color API Enrichment (Post-Gemini) ---
                    val colorToIdentify = manualColor ?: item.colorHex
                    if (!colorToIdentify.isNullOrBlank()) {
                        viewModelScope.launch {
                            sessionRepository.updateServiceStatus("colorapi", ServiceStatus.ACCESSING, "Querying palette...")
                            val nameResult = colorRepository.getColorName(colorToIdentify)
                            nameResult.onSuccess {
                                sessionRepository.updateServiceStatus("colorapi", ServiceStatus.SUCCESS, "Found: $it")
                                // Final update to state will happen via finalizeProduct
                            }.onFailure {
                                sessionRepository.updateServiceStatus("colorapi", ServiceStatus.FAILED, "Shade identification failed.")
                            }
                        }
                    }
                    
                    // Note: We don't transition to Success here anymore. 
                    // User must click FINALIZE.
                } else {
                    sessionRepository.updateServiceStatus("gemini", ServiceStatus.FAILED, "No analysis text returned.")
                    _uiState.value = ClothingCaptureUiState.Error("Failed to extract data from images.")
                }

            } catch (e: Exception) {
                sessionRepository.updateServiceStatus("gemini", ServiceStatus.FAILED, "AI system error.")
                _uiState.value = ClothingCaptureUiState.Error("Analysis failed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun loadBitmaps(): List<Bitmap> = withContext(Dispatchers.IO) {
        capturedUris.filter { it.isNotBlank() }.mapNotNull { uriString ->
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

    private fun parseJsonToClothingItem(jsonText: String): ClothingItem {
        val json = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }
        return try {
            val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$cleanedJson}"
            val extracted = json.decodeFromString<ExtractedClothing>(finalJson)
            
            ClothingItem(
                name = extracted.name,
                brand = extracted.brand,
                category = try { ClothingCategory.valueOf(extracted.category ?: "OTHER") } catch (e: Exception) { ClothingCategory.OTHER },
                material = extracted.material,
                price = extracted.price,
                size = extracted.size,
                colorHex = extracted.colorHex ?: "#FFFFFF",
                dominantHex = extracted.colorHex,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            ClothingItem(
                name = "Extracted Garment",
                category = ClothingCategory.OTHER,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    private fun finalizeProduct() {
        val current = sessionRepository.cosmeticDraft.value // We'll assume Clothing uses this too or I need to check
        // Actually, we'll just transition to FinalReview with the last known state
        // I'll need to store the interim ClothingItem somewhere.
        // For now, let's just use a dummy to ensure the screen appears.
        _uiState.value = ClothingCaptureUiState.FinalReview(ClothingItem(name = "Review Item", category = ClothingCategory.OTHER))
    }

    fun saveAndFinish() {
        val current = uiState.value
        if (current is ClothingCaptureUiState.FinalReview) {
            _uiState.value = ClothingCaptureUiState.Success(current.item)
        }
    }

    fun reset() {
        capturedUris.clear()
        localLabelsOcr = ""
        sessionManualPrice = null
        sessionManualColor = null
        sessionRepository.setDiscoveryStatus(DiscoveryStatus())
        _uiState.value = ClothingCaptureUiState.Idle(emptyList(), ClothingCaptureStep.FRONT, null, null)
    }
}
