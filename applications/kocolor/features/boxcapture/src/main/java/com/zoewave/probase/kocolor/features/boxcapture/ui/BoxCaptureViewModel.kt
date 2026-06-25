package com.zoewave.probase.kocolor.features.boxcapture.ui

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
import kotlinx.coroutines.flow.firstOrNull
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
    val volume: String? = null,
    
    // Advanced Extraction
    val ingredients: List<String> = emptyList(),
    val heroIngredient: String? = null,
    val skinCompatibility: String? = null,
    val containsFragrance: Boolean? = null,
    val ecoScore: String? = null,
    val isVegan: Boolean? = null,
    val isCrueltyFree: Boolean? = null,
    val recyclingInstructions: String? = null,
    val fdaClinicalWarnings: List<String> = emptyList(),
    val fdaActiveIngredients: List<String> = emptyList()
)

@HiltViewModel
class BoxCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiSettings: AiConfigurationSettings,
    private val repository: CosmeticInventoryRepository,
    private val localAnalyzer: LocalProductAnalyzer
) : ViewModel() {

    companion object {
        private const val TAG = "BoxCaptureViewModel"
    }

    private val _uiState = MutableStateFlow<BoxCaptureUiState>(BoxCaptureUiState.Idle())
    val uiState: StateFlow<BoxCaptureUiState> = _uiState.asStateFlow()

    private val capturedUris = mutableListOf<String>()
    private var scannedBarcode: String? = null

    fun onEvent(event: BoxCaptureEvent) {
        when (event) {
            is BoxCaptureEvent.Capture -> onPhotoCaptured(event.uri)
            is BoxCaptureEvent.BarcodeScanned -> onBarcodeScanned(event.code)
            is BoxCaptureEvent.UpdateDraft -> onUpdateDraft(event.item)
            BoxCaptureEvent.ConfirmSave -> onConfirmSave()
            BoxCaptureEvent.Retry -> reset()
            BoxCaptureEvent.Dismiss -> { /* Handled in UI layer typically */ }
            is BoxCaptureEvent.Success -> { /* Handled in UI layer typically */ }
        }
    }

    private fun onUpdateDraft(item: CosmeticItem) {
        _uiState.value = BoxCaptureUiState.Reviewing(item)
    }

    private fun onConfirmSave() {
        val item = (uiState.value as? BoxCaptureUiState.Reviewing)?.item ?: return
        viewModelScope.launch {
            val savedId = repository.saveCosmeticItem(item)
            _uiState.value = BoxCaptureUiState.Success(item.copy(id = savedId))
        }
    }

    fun setMode(modeString: String) {
        val mode = try { CaptureMode.valueOf(modeString) } catch (e: Exception) { CaptureMode.BOX }
        reset() // Ensure clean state when switching modes
        _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), CaptureStep.getStepsForMode(mode).first(), mode)
    }

    private fun onPhotoCaptured(uri: String) {
        capturedUris.add(uri)
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
        val nextStep = getNextStep(mode)
        if (nextStep != null) {
            _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), nextStep, mode)
        } else {
            analyzePhotos(mode)
        }
    }

    private fun onBarcodeScanned(code: String) {
        scannedBarcode = code
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.QUICK_BOX
        analyzePhotos(mode)
    }

    private fun getNextStep(mode: CaptureMode): CaptureStep? {
        val steps = CaptureStep.getStepsForMode(mode)
        val currentSize = capturedUris.size
        return steps.getOrNull(currentSize)
    }

    fun analyzePhotos(mode: CaptureMode) {
        viewModelScope.launch {
            val apiKey = aiSettings.getGeminiApiKey()
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"
            
            Log.d(TAG, "Starting analysis. Mode: $mode, Model: $modelName, Barcode: $scannedBarcode")

            if (apiKey.isNullOrBlank()) {
                Log.w(TAG, "API Key is missing. Falling back to local analysis.")
                runLocalAnalysis()
                return@launch
            }

            _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Processing Images...")
            
            try {
                val bitmaps = loadBitmaps()
                Log.d(TAG, "Loaded ${bitmaps.size} bitmaps for analysis.")
                
                if (bitmaps.isEmpty()) {
                    Log.e(TAG, "No valid images captured.")
                    _uiState.value = BoxCaptureUiState.Error("No valid images captured.")
                    return@launch
                }

                // --- Hybrid Analysis: Local OCR for Ingredients ---
                var localIngredientsOcr = ""
                val ingredientsIndex = if (mode == CaptureMode.QUICK_BOX) 2 else 6 // Specific steps
                if (capturedUris.size > ingredientsIndex) {
                    val ingredientsBitmap = loadBitmapFromUri(Uri.parse(capturedUris[ingredientsIndex]))
                    if (ingredientsBitmap != null) {
                        Log.d(TAG, "Running local OCR on ingredients panel...")
                        localIngredientsOcr = localAnalyzer.extractText(ingredientsBitmap)
                    }
                }

                val model = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                    }
                )

                val prompt = content {
                    bitmaps.forEach { image(it) }
                    val target = when (mode) {
                        CaptureMode.BOX -> "product box from all sides"
                        CaptureMode.QUICK_BOX -> "essential product box angles"
                        CaptureMode.PRODUCT -> "product container (front and back)"
                    }
                    val barcodeContext = if (!scannedBarcode.isNullOrBlank()) "The scanned barcode is: $scannedBarcode." else ""
                    val ocrContext = if (localIngredientsOcr.isNotBlank()) "LOCAL OCR EXTRACTED TEXT FROM INGREDIENTS PANEL:\n$localIngredientsOcr\n---" else ""
                    
                    text("""
                        Analyze these photos of a $target. $barcodeContext
                        $ocrContext
                        
                        Extract all available information to fill a professional cosmetic database entry. Use the OCR text above to help identify specific ingredients correctly.
                        Return ONLY the following JSON format:
                        {
                            "name": "Product Name",
                            "brand": "Brand Name",
                            "macroCategory": "PREP|COMPLEXION|DIMENSION|EYES|LIPS|HAIR|HYGIENE|ORAL|FRAGRANCE|GROOMING|TOOLS",
                            "microCategory": "FOUNDATION|SPF|SERUM|CLEANSER|MOISTURIZER|TONER|LIPSTICK|etc",
                            "formulation": "LIQUID|CREAM|POWDER|GEL|STICK|PENCIL|BALM|OIL|SPRAY|FOAM|LOOSE_POWDER|PRESSED_POWDER|UNKNOWN",
                            "chemistryBase": "WATER|SILICONE|OIL|ALCOHOL|MINERAL|WAX|HYBRID|UNKNOWN",
                            "finish": "MATTE|SATIN|NATURAL|DEWY|GLOSSY|SHIMMER|METALLIC|SHEER|VELVET|UNKNOWN",
                            "coverage": "SHEER|LIGHT|MEDIUM|FULL|BUILDABLE|NOT_APPLICABLE",
                            "shadeName": "Shade name",
                            "colorHex": "#RRGGBB",
                            "instructions": "Usage instructions for the application guide",
                            "batchCode": "Barcode or batch code",
                            "paoMonths": 12,
                            "volume": "30ml",
                            "ingredients": ["Water", "Glycerin", "..."],
                            "heroIngredient": "Main active ingredient",
                            "skinCompatibility": "e.g. Sensitive, Oily, All Skin Types",
                            "containsFragrance": true,
                            "ecoScore": "A|B|C|D|E",
                            "isVegan": true,
                            "isCrueltyFree": true,
                            "recyclingInstructions": "How to recycle the packaging",
                            "fdaClinicalWarnings": ["Warning 1", "..."],
                            "fdaActiveIngredients": ["Active 1", "..."]
                        }
                        Be extremely precise. If a field is unknown, use null.
                    """.trimIndent())
                }

                Log.d(TAG, "Sending request to Gemini...")
                val response = model.generateContent(prompt)
                val jsonText = response.text
                
                if (jsonText != null) {
                    Log.d(TAG, "Received response from Gemini: $jsonText")
                    var item = parseJsonToCosmeticItem(jsonText)
                    
                    // Pre-fill Front Image and ensure Barcode is set
                    item = item.copy(
                        imageUrl = capturedUris.firstOrNull(),
                        batchCode = scannedBarcode ?: item.batchCode
                    )

                    _uiState.value = BoxCaptureUiState.Reviewing(item)
                } else {
                    Log.e(TAG, "Gemini returned null text in response.")
                    _uiState.value = BoxCaptureUiState.Error("Failed to extract data from images.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Gemini Analysis Exception", e)
                val errorMsg = when {
                    e.message?.contains("404") == true -> "The AI model is currently unavailable or the model name is incorrect. Switching to local analysis..."
                    e.message?.contains("MissingFieldException") == true -> "AI communication error. Please try again or use local analysis."
                    else -> "Analysis failed: ${e.localizedMessage ?: "Unknown error"}"
                }
                
                if (e.message?.contains("404") == true) {
                    Log.i(TAG, "Triggering auto-fallback to local analysis due to 404.")
                    runLocalAnalysis()
                } else {
                    _uiState.value = BoxCaptureUiState.Error(errorMsg)
                }
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
                macroCategory = try { MacroCategory.valueOf(extracted.macroCategory ?: "PREP") } catch (e: Exception) { MacroCategory.PREP },
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
                timestamp = System.currentTimeMillis(),
                
                // Advanced fields
                ingredients = extracted.ingredients,
                heroIngredient = extracted.heroIngredient,
                skinCompatibility = extracted.skinCompatibility,
                containsFragrance = extracted.containsFragrance,
                ecoScore = extracted.ecoScore,
                isVegan = extracted.isVegan,
                isCrueltyFree = extracted.isCrueltyFree,
                recyclingInstructions = extracted.recyclingInstructions,
                fdaClinicalWarnings = extracted.fdaClinicalWarnings,
                fdaActiveIngredients = extracted.fdaActiveIngredients,
                isFdaChecked = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
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
