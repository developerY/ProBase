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
import com.zoewave.probase.core.model.ritual.ChemistryBase
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Coverage
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Formulation
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.features.boxcapture.data.LocalProductAnalyzer
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.BoxCaptureUiState
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureMode
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.CaptureStep
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
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val sessionRepository: FashionSessionRepository,
    private val localAnalyzer: LocalProductAnalyzer
) : ViewModel() {

    companion object {
        private const val TAG = "BoxCaptureViewModel"
    }

    private val _uiState = MutableStateFlow<BoxCaptureUiState>(BoxCaptureUiState.Idle())
    val uiState: StateFlow<BoxCaptureUiState> = _uiState.asStateFlow()

    private val capturedUris = mutableListOf<String>()
    private var scannedBarcode: String? = null
    private var obfEnrichmentData: CosmeticItem? = null
    private var localIngredientsOcr: String = ""
    private var localInstructionsOcr: String = ""

    fun onEvent(event: BoxCaptureEvent) {
        when (event) {
            is BoxCaptureEvent.Capture -> onPhotoCaptured(event.uri)
            is BoxCaptureEvent.BarcodeScanned -> onBarcodeScanned(event.code)
            BoxCaptureEvent.Retry -> reset()
            BoxCaptureEvent.Dismiss -> { /* Handled in UI layer typically */ }
            is BoxCaptureEvent.Success -> { /* Handled in UI layer typically */ }
            is BoxCaptureEvent.DeletePhoto -> {
                if (event.index in capturedUris.indices) {
                    capturedUris.removeAt(event.index)
                    refreshStep()
                }
            }
            is BoxCaptureEvent.ChangeMode -> {
                val currentMode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
                if (currentMode != event.mode) {
                    // Only reset if we actually change
                    capturedUris.clear()
                    _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), CaptureStep.getStepsForMode(event.mode).first(), event.mode)
                }
            }
            BoxCaptureEvent.SubmitToAi -> {
                val mode = (uiState.value as? BoxCaptureUiState.Review)?.mode ?: CaptureMode.BOX
                analyzePhotos(mode)
            }
            BoxCaptureEvent.SkipBarcode -> {
                val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
                prepareReview(mode)
            }
            BoxCaptureEvent.SkipStep -> skipStep()
            is BoxCaptureEvent.OnColorSelected -> {
                val current = (uiState.value as? BoxCaptureUiState.Idle)
                if (current != null) {
                    _uiState.value = current.copy(extractedColorHex = event.hex)
                } else if (uiState.value is BoxCaptureUiState.ColorConfirmation) {
                    val cc = uiState.value as BoxCaptureUiState.ColorConfirmation
                    _uiState.value = cc.copy(suggestedColorHex = event.hex)
                }
            }
            BoxCaptureEvent.ConfirmColor -> {
                val current = (uiState.value as? BoxCaptureUiState.ColorConfirmation) ?: return
                val nextStep = getNextStep(current.mode)
                if (nextStep != null) {
                    _uiState.value = BoxCaptureUiState.Idle(current.capturedUris, nextStep, current.mode, current.suggestedColorHex)
                } else {
                    prepareReview(current.mode)
                }
            }
            BoxCaptureEvent.ClearColor -> {
                val current = (uiState.value as? BoxCaptureUiState.ColorConfirmation) ?: return
                val nextStep = getNextStep(current.mode)
                if (nextStep != null) {
                    _uiState.value = BoxCaptureUiState.Idle(current.capturedUris, nextStep, current.mode, null)
                } else {
                    prepareReview(current.mode)
                }
            }
        }
    }

    private fun skipStep() {
        val current = (uiState.value as? BoxCaptureUiState.Idle) ?: return
        if (current.currentStep.isSkippable) {
            capturedUris.add("") 
            
            if (current.currentStep == CaptureStep.COLOR) {
                // Moving past COLOR without a photo
                val nextStep = getNextStep(current.mode)
                if (nextStep != null) {
                    _uiState.value = current.copy(capturedUris = capturedUris.toList(), currentStep = nextStep)
                } else {
                    prepareReview(current.mode)
                }
                return
            }

            val nextStep = getNextStep(current.mode)
            if (nextStep != null) {
                _uiState.value = current.copy(capturedUris = capturedUris.toList(), currentStep = nextStep)
            } else {
                prepareReview(current.mode)
            }
        }
    }

    private fun refreshStep() {
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: 
                   (uiState.value as? BoxCaptureUiState.Review)?.mode ?:
                   (uiState.value as? BoxCaptureUiState.ColorConfirmation)?.mode ?:
                   CaptureMode.BOX
        
        val currentStep = getNextStep(mode) ?: CaptureStep.BARCODE // Fallback
        _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), currentStep, mode)
    }

    fun setMode(modeString: String) {
        val mode = try { CaptureMode.valueOf(modeString) } catch (e: Exception) { CaptureMode.BOX }
        
        // Peek at existing draft for enrichment context
        val existingDraft = sessionRepository.cosmeticDraft.value
        val hasExistingBarcode = !existingDraft?.batchCode.isNullOrBlank()
        
        reset(keepBarcode = hasExistingBarcode) 
        
        if (hasExistingBarcode) {
            scannedBarcode = existingDraft.batchCode
            obfEnrichmentData = existingDraft
        }

        _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), CaptureStep.getStepsForMode(mode).first(), mode)
    }

    private fun onPhotoCaptured(uri: String) {
        capturedUris.add(uri)
        val current = (uiState.value as? BoxCaptureUiState.Idle) ?: return
        val mode = current.mode
        
        if (current.currentStep == CaptureStep.COLOR) {
            // Auto-extract color from the photo
            viewModelScope.launch {
                val bitmap = loadBitmapFromUri(Uri.parse(uri))
                val colorHex = if (bitmap != null) localAnalyzer.extractDominantColor(bitmap) else "#808080"
                _uiState.value = BoxCaptureUiState.ColorConfirmation(
                    capturedUris = capturedUris.toList(),
                    suggestedColorHex = colorHex,
                    mode = mode
                )
            }
            return
        }

        val nextStep = getNextStep(mode)
        if (nextStep != null) {
            _uiState.value = current.copy(capturedUris = capturedUris.toList(), currentStep = nextStep)
        } else {
            prepareReview(mode)
        }
    }

    private fun onBarcodeScanned(code: String) {
        scannedBarcode = code
        viewModelScope.launch {
            // First attempt to find in online database to skip AI if possible
            _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Searching online database...")
            
            val result = cosmeticRepository.fetchProductByBarcode(code)
            result.onSuccess { obfItem ->
                // Confidence Check: Do we have ingredients?
                val hasIngredients = obfItem.ingredients.isNotEmpty()
                val isComplete = hasIngredients && obfItem.name.isNotBlank() && obfItem.brand.isNotBlank()

                if (isComplete) {
                    Log.d(TAG, "Product found in OBF with high confidence! Skipping Gemini analysis.")
                    val item = obfItem.copy(
                        imageUrl = capturedUris.firstOrNull { it.isNotBlank() },
                        batchCode = code
                    )
                    sessionRepository.setCosmeticDraft(item)
                    _uiState.value = BoxCaptureUiState.Success(item)
                } else {
                    Log.i(TAG, "Product found in OBF but incomplete (missing ingredients). Proceeding to AI enrichment.")
                    obfEnrichmentData = obfItem
                    val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
                    prepareReview(mode)
                }
            }.onFailure {
                Log.i(TAG, "Product not found in OBF. Proceeding to AI review.")
                obfEnrichmentData = null
                val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
                prepareReview(mode)
            }
        }
    }

    private fun prepareReview(mode: CaptureMode) {
        viewModelScope.launch {
            _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Performing Local OCR...")
            
            // Re-map capturedUris to steps to find the right index
            val steps = CaptureStep.getStepsForMode(mode)
            val ingredientsIdx = steps.indexOf(CaptureStep.INGREDIENTS)
            val instructionsIdx = steps.indexOf(CaptureStep.INSTRUCTIONS)
            val colorIdx = steps.indexOf(CaptureStep.COLOR)

            localIngredientsOcr = if (ingredientsIdx != -1 && ingredientsIdx in capturedUris.indices) {
                val uri = capturedUris[ingredientsIdx]
                if (uri.isNotBlank()) {
                    loadBitmapFromUri(Uri.parse(uri))?.let { localAnalyzer.extractText(it) } ?: ""
                } else ""
            } else ""

            localInstructionsOcr = if (instructionsIdx != -1 && instructionsIdx in capturedUris.indices) {
                val uri = capturedUris[instructionsIdx]
                if (uri.isNotBlank()) {
                    loadBitmapFromUri(Uri.parse(uri))?.let { localAnalyzer.extractText(it) } ?: ""
                } else ""
            } else ""

            val manualColor = (uiState.value as? BoxCaptureUiState.Idle)?.extractedColorHex

            _uiState.value = BoxCaptureUiState.Review(
                capturedUris = capturedUris.toList(),
                barcode = scannedBarcode,
                ingredientsOcr = localIngredientsOcr,
                instructionsOcr = localInstructionsOcr,
                mode = mode,
                enrichmentData = obfEnrichmentData,
                manualColorHex = manualColor
            )
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
            val modelName = aiSettings.aiModelFlow.firstOrNull() ?: "gemini-1.5-flash"
            
            val currentReview = (uiState.value as? BoxCaptureUiState.Review)
            val manualColor = currentReview?.manualColorHex

            Log.d(TAG, "Starting analysis. Mode: $mode, Model: $modelName, Barcode: $scannedBarcode, Color: $manualColor")

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
                        CaptureMode.BOX -> "product box from multiple sides"
                        CaptureMode.PRODUCT -> "product container (front and back)"
                    }
                    val barcodeContext = if (!scannedBarcode.isNullOrBlank()) "The scanned barcode is: $scannedBarcode." else ""
                    val colorContext = if (!manualColor.isNullOrBlank()) "The user-selected product color is: $manualColor." else "Please identify the best representative color for this product from the photos."
                    
                    val enrichmentContext = obfEnrichmentData?.let {
                        """
                        CONFIRMED DATABASE INFO:
                        - Brand: ${it.brand}
                        - Name: ${it.name}
                        - Category: ${it.macroCategory.displayName} / ${it.microCategory.displayName}
                        Please focus your visual analysis on extracting the MISSING details like full ingredients and specific instructions.
                        """.trimIndent()
                    } ?: ""

                    val ingredientsContext = if (localIngredientsOcr.isNotBlank()) "LOCAL OCR EXTRACTED TEXT FROM INGREDIENTS PANEL:\n$localIngredientsOcr\n---" else ""
                    val instructionsContext = if (localInstructionsOcr.isNotBlank()) "LOCAL OCR EXTRACTED TEXT FROM INSTRUCTIONS/BACK PANEL:\n$localInstructionsOcr\n---" else ""
                    
                    text("""
                        Analyze these photos of a $target. $barcodeContext
                        $enrichmentContext
                        $colorContext
                        $ingredientsContext
                        $instructionsContext
                        
                        Extract all available information to fill a professional cosmetic database entry. Use the confirmed database info and OCR text above to help identify specific ingredients and instructions correctly.
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
                    
                    // Pre-fill Front Image and ensure Barcode/Color is set
                    item = item.copy(
                        imageUrl = capturedUris.firstOrNull { it.isNotBlank() },
                        batchCode = scannedBarcode ?: item.batchCode,
                        colorHex = manualColor ?: item.colorHex
                    )

                    sessionRepository.setCosmeticDraft(item)
                    _uiState.value = BoxCaptureUiState.Success(item)
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
            sessionRepository.setCosmeticDraft(item)
            _uiState.value = BoxCaptureUiState.Success(item)
        } catch (e: Exception) {
            _uiState.value = BoxCaptureUiState.Error("Local analysis failed: ${e.localizedMessage}")
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

    fun reset(keepBarcode: Boolean = false) {
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: 
                   (uiState.value as? BoxCaptureUiState.Review)?.mode ?:
                   CaptureMode.BOX
        capturedUris.clear()
        if (!keepBarcode) {
            scannedBarcode = null
            obfEnrichmentData = null
        }
        localIngredientsOcr = ""
        localInstructionsOcr = ""
        _uiState.value = BoxCaptureUiState.Idle(emptyList(), CaptureStep.getStepsForMode(mode).first(), mode)
    }
}
