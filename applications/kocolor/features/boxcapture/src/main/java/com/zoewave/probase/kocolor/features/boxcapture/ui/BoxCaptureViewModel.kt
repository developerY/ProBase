package com.zoewave.probase.kocolor.features.boxcapture.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.model.network.DiscoveryStatus
import com.zoewave.probase.core.model.network.ServiceStatus
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.features.ai.local.data.LocalAiEngine
import com.zoewave.probase.features.ai.local.data.LocalStandardizedData
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.usecase.DeterministicApiMetadata
import com.zoewave.probase.kocolor.data.usecase.ResolveProductUseCase
import com.zoewave.probase.kocolor.data.worker.EnrichmentWorker
import com.zoewave.probase.kocolor.db.dao.ProductDao
import com.zoewave.probase.kocolor.db.entity.EnrichmentStatus
import com.zoewave.probase.kocolor.db.entity.ProductEntity
import com.zoewave.probase.kocolor.features.analyzer.data.LocalProductAnalyzer
import com.zoewave.probase.kocolor.features.boxcapture.ui.state.*
import com.zoewave.probase.kocolor.features.chemicals.domain.repository.ChemicalRepository
import com.zoewave.probase.kocolor.features.colors.domain.repository.ColorRepository
import com.zoewave.probase.kocolor.features.fda.data.repository.FdaRepository
import com.zoewave.probase.kocolor.features.makeupapi.domain.repository.MakeupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BoxCaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aiSettings: AiConfigurationSettings,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val sessionRepository: FashionSessionRepository,
    private val localAnalyzer: LocalProductAnalyzer,
    private val localAi: LocalAiEngine,
    private val ocrEngine: LocalOcrEngine,
    private val fdaRepository: FdaRepository,
    private val chemicalRepository: ChemicalRepository,
    private val colorRepository: ColorRepository,
    private val makeupRepository: MakeupRepository,
    private val resolveProductUseCase: ResolveProductUseCase,
    private val productDao: ProductDao,
    private val workManager: WorkManager
) : ViewModel() {

    companion object {
        private const val TAG = "BoxCaptureViewModel"
    }

    val discoveryStatus = sessionRepository.discoveryStatus

    private val _uiState = MutableStateFlow<BoxCaptureUiState>(BoxCaptureUiState.Idle())
    val uiState: StateFlow<BoxCaptureUiState> = _uiState.asStateFlow()

    private val capturedUris = mutableListOf<String>()
    private var scannedBarcode: String? = null
    private var sessionManualPrice: Double? = null
    private var sessionManualColor: String? = null

    // Stage 3.5 & 3.75 Resolution Anchors
    private var localAiStandardizedData: LocalStandardizedData? = null
    private var deterministicApiMetadata = DeterministicApiMetadata()

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Processing)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    fun onEvent(event: BoxCaptureEvent) {
        when (event) {
            is BoxCaptureEvent.Capture -> onPhotoCaptured(event.uri)
            is BoxCaptureEvent.BarcodeScanned -> onBarcodeScanned(event.code)
            BoxCaptureEvent.Retry -> reset()
            BoxCaptureEvent.Dismiss -> { /* Handled in UI layer */ }
            is BoxCaptureEvent.Success -> { /* Handled in UI layer */ }
            is BoxCaptureEvent.DeletePhoto -> {
                if (event.index in capturedUris.indices) {
                    capturedUris.removeAt(event.index)
                    refreshStep()
                }
            }
            is BoxCaptureEvent.ChangeMode -> {
                val currentMode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
                if (currentMode != event.mode) {
                    capturedUris.clear()
                    _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), CaptureStep.getStepsForMode(event.mode).first(), event.mode)
                }
            }
            BoxCaptureEvent.SubmitToAi -> { /* Backgrounded via WorkManager */ }
            BoxCaptureEvent.SkipBarcode -> {
                viewModelScope.launch { resolveAndSave() }
            }
            BoxCaptureEvent.SkipStep -> skipStep()
            BoxCaptureEvent.ContinueToReview -> {
                viewModelScope.launch { resolveAndSave() }
            }
            BoxCaptureEvent.FinalizeProduct -> { /* Handled by resolveAndSave */ }
            BoxCaptureEvent.SaveProduct -> saveAndFinish()
            is BoxCaptureEvent.OnColorSelected -> {
                sessionManualColor = event.hex
                val current = (uiState.value as? BoxCaptureUiState.Idle)
                if (current != null) {
                    _uiState.value = current.copy(extractedColorHex = event.hex)
                }
            }
            BoxCaptureEvent.ConfirmColor -> {
                val current = (uiState.value as? BoxCaptureUiState.ColorConfirmation) ?: return
                sessionManualColor = current.selectedColorHex
                val nextStep = getNextStep(current.mode)
                if (nextStep != null) {
                    _uiState.value = BoxCaptureUiState.Idle(current.capturedUris, nextStep, current.mode, sessionManualColor, sessionManualPrice)
                } else {
                    startDiscovery(current.mode)
                }
            }
            BoxCaptureEvent.ClearColor -> {
                sessionManualColor = null
                val current = (uiState.value as? BoxCaptureUiState.ColorConfirmation) ?: return
                val nextStep = getNextStep(current.mode)
                if (nextStep != null) {
                    _uiState.value = BoxCaptureUiState.Idle(current.capturedUris, nextStep, current.mode, null, sessionManualPrice)
                } else {
                    startDiscovery(current.mode)
                }
            }
            BoxCaptureEvent.ConfirmPrice -> {
                val current = (uiState.value as? BoxCaptureUiState.PriceConfirmation) ?: return
                sessionManualPrice = current.detectedPrice
                val nextStep = getNextStep(current.mode)
                if (nextStep != null) {
                    _uiState.value = BoxCaptureUiState.Idle(current.capturedUris, nextStep, current.mode, sessionManualColor, sessionManualPrice)
                } else {
                    startDiscovery(current.mode)
                }
            }
            is BoxCaptureEvent.OnPriceChanged -> {
                sessionManualPrice = event.price
                val current = (uiState.value as? BoxCaptureUiState.PriceConfirmation)
                if (current != null) {
                    _uiState.value = current.copy(detectedPrice = event.price)
                }
            }
        }
    }

    private fun startDiscovery(mode: CaptureMode) {
        viewModelScope.launch {
            _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Orchestrating Discovery...", mode)
            if (scannedBarcode == null) {
                triggerLocalDiscovery(mode)
            }
        }
    }

    private fun triggerLocalDiscovery(mode: CaptureMode) {
        Log.d(TAG, "Starting Local Discovery for mode: $mode")
        viewModelScope.launch {
            sessionRepository.updateServiceStatus("localai", ServiceStatus.ACCESSING, "Synthesizing OCR text...")
            val bitmaps = loadBitmaps()
            Log.d(TAG, "Running OCR on ${bitmaps.size} bitmaps...")
            val ocrText = ocrEngine.extractTextFromBitmaps(bitmaps)
            Log.d(TAG, "OCR complete. Total text length: ${ocrText.length}")
            
            val localAiResult = localAi.standardizeOcrText(ocrText)
            
            localAiResult.onSuccess { data ->
                Log.d(TAG, "Local AI Success. Brand: ${data.brand}, Name: ${data.productName}")
                localAiStandardizedData = data
                sessionRepository.updateServiceStatus("localai", ServiceStatus.SUCCESS, "Found: ${data.brand}")
                if (!data.brand.isNullOrBlank()) {
                    startBackgroundEnrichment(data.brand!!, data.productName ?: "", scannedBarcode ?: "", data.ingredients.firstOrNull())
                }
            }.onFailure { e ->
                Log.w(TAG, "Local AI Failed: ${e.message}")
                sessionRepository.updateServiceStatus("localai", ServiceStatus.UNSUPPORTED, "Hardware bypass active.")
                val fallbackBrand = extractBrandFromText(ocrText)
                Log.d(TAG, "Heuristic fallback brand: $fallbackBrand")
                localAiStandardizedData = LocalStandardizedData(brand = fallbackBrand)
                if (!fallbackBrand.isNullOrBlank()) {
                    startBackgroundEnrichment(fallbackBrand, "", scannedBarcode ?: "", null)
                }
            }
        }
    }

    private fun onBarcodeScanned(code: String) {
        scannedBarcode = code
        Log.d(TAG, "Barcode Scanned: $code. Triggering OBF lookup...")
        viewModelScope.launch {
            val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
            _uiState.value = BoxCaptureUiState.Analyzing(capturedUris.toList(), "Querying Database...", mode)
            
            sessionRepository.updateServiceStatus("obf", ServiceStatus.ACCESSING, "Querying barcode: $code")
            val obfResult = cosmeticRepository.fetchProductByBarcode(code)
            
            obfResult.onSuccess { obfItem ->
                Log.d(TAG, "OBF Success: ${obfItem.name}. Brand: ${obfItem.brand}")
                sessionRepository.updateServiceStatus("obf", ServiceStatus.SUCCESS, "Found: ${obfItem.name}")
                deterministicApiMetadata = DeterministicApiMetadata(obfItem.brand, obfItem.name, obfItem.ingredients)
                sessionRepository.updateServiceStatus("localai", ServiceStatus.SUCCESS, "Using OBF context.")
                startBackgroundEnrichment(obfItem.brand, obfItem.name, code, obfItem.ingredients.firstOrNull())
            }.onFailure {
                Log.d(TAG, "OBF Failure for $code. Falling back to Local AI...")
                sessionRepository.updateServiceStatus("obf", ServiceStatus.FAILED, "Barcode not in OBF.")
                triggerLocalDiscovery(mode)
            }
        }
    }

    private suspend fun resolveAndSave() {
        Log.d(TAG, "Resolving and Saving product...")
        val localData = localAiStandardizedData ?: LocalStandardizedData()
        
        // Stage 3.5 & 3.75 Resolution
        val productEntity = resolveProductUseCase.execute(localData, deterministicApiMetadata)
        Log.d(TAG, "Resolution complete. Final Brand: ${productEntity.brand}, Confidence: ${productEntity.deterministicConfidence}")
        
        // Stage 4: Room 3 Database Persistence
        val productId = productDao.insertProduct(productEntity)
        Log.d(TAG, "Product saved to Room 3. ID: $productId. Queueing background enrichment...")
        
        // Stage 4: WorkManager Queue
        queueEnrichment(productId)
        
        _discoveryState.value = DiscoveryState.LocalSuccess(localData)
        _uiState.value = BoxCaptureUiState.FinalReview(productEntity.copy(id = productId))
    }

    private fun queueEnrichment(productId: Long) {
        val data = Data.Builder().putLong("product_id", productId).build()
        val request = OneTimeWorkRequestBuilder<EnrichmentWorker>().setInputData(data).build()
        workManager.enqueue(request)
    }

    private fun startBackgroundEnrichment(brand: String, name: String, barcode: String, topIngredient: String?) {
        Log.d(TAG, "Starting parallel enrichment for Brand: $brand, Name: $name, Barcode: $barcode")
        viewModelScope.launch {
            sessionRepository.updateServiceStatus("fda", ServiceStatus.ACCESSING, "Checking clinical status...")
            val recall = fdaRepository.getRecalls(brand, name)
            if (recall != null) {
                Log.d(TAG, "FDA Success: Recall found.")
                sessionRepository.updateServiceStatus("fda", ServiceStatus.SUCCESS, "Safety data verified.")
            } else {
                Log.d(TAG, "FDA: No recall found.")
                sessionRepository.updateServiceStatus("fda", ServiceStatus.FAILED, "No clinical data found.")
            }
        }
        viewModelScope.launch {
            if (topIngredient != null) {
                sessionRepository.updateServiceStatus("chemdb", ServiceStatus.ACCESSING, "Analyzing $topIngredient...")
                val chemicalInfo = chemicalRepository.getChemicalInfo(topIngredient).getOrNull()
                if (chemicalInfo != null) {
                    Log.d(TAG, "chemDB Success: Hazards found.")
                    sessionRepository.updateServiceStatus("chemdb", ServiceStatus.SUCCESS, "Hazards identified.")
                } else {
                    Log.d(TAG, "chemDB: No hazard data.")
                    sessionRepository.updateServiceStatus("chemdb", ServiceStatus.FAILED, "Ingredient hazards unknown.")
                }
            } else {
                Log.d(TAG, "chemDB: No ingredient context.")
                sessionRepository.updateServiceStatus("chemdb", ServiceStatus.FAILED, "No context.")
            }
        }
        viewModelScope.launch {
            sessionRepository.updateServiceStatus("makeupapi", ServiceStatus.ACCESSING, "Matching brand: $brand")
            val catalogResult = makeupRepository.searchProducts(brand = brand)
            if (catalogResult.isSuccess) {
                Log.d(TAG, "MakeupAPI Success.")
                sessionRepository.updateServiceStatus("makeupapi", ServiceStatus.SUCCESS, "Matched catalog.")
            } else {
                Log.d(TAG, "MakeupAPI: Brand not found.")
                sessionRepository.updateServiceStatus("makeupapi", ServiceStatus.FAILED, "Brand not in catalog.")
            }
        }
    }

    private fun extractBrandFromText(text: String): String? {
        return if (text.isNotBlank()) text.split(" ").firstOrNull { it.length > 2 && it[0].isUpperCase() } else null
    }

    fun saveAndFinish() {
        val current = uiState.value
        if (current is BoxCaptureUiState.FinalReview) {
            _uiState.value = BoxCaptureUiState.Success(CosmeticItem(
                name = current.item.productName,
                brand = current.item.brand,
                macroCategory = MacroCategory.COMPLEXION,
                microCategory = MicroCategory.FOUNDATION
            ))
        }
    }

    private fun skipStep() {
        val current = (uiState.value as? BoxCaptureUiState.Idle) ?: return
        if (current.currentStep.isSkippable) {
            capturedUris.add("") 
            val nextStep = getNextStep(current.mode)
            if (nextStep != null) {
                _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), nextStep, current.mode, sessionManualColor, sessionManualPrice)
            } else {
                startDiscovery(current.mode)
            }
        }
    }

    private fun refreshStep() {
        val mode = (uiState.value as? BoxCaptureUiState.Idle)?.mode ?: CaptureMode.BOX
        val currentStep = getNextStep(mode) ?: CaptureStep.BARCODE
        _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), currentStep, mode, sessionManualColor, sessionManualPrice)
    }

    fun setMode(modeString: String) {
        val mode = try { CaptureMode.valueOf(modeString) } catch (e: Exception) { CaptureMode.BOX }
        reset()
        _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), CaptureStep.getStepsForMode(mode).first(), mode)
    }

    private fun onPhotoCaptured(uri: String) {
        capturedUris.add(uri)
        val current = (uiState.value as? BoxCaptureUiState.Idle) ?: return
        val nextStep = getNextStep(current.mode)
        if (nextStep != null) {
            _uiState.value = BoxCaptureUiState.Idle(capturedUris.toList(), nextStep, current.mode, sessionManualColor, sessionManualPrice)
        } else {
            startDiscovery(current.mode)
        }
    }

    private fun getNextStep(mode: CaptureMode): CaptureStep? {
        val steps = CaptureStep.getStepsForMode(mode)
        return steps.getOrNull(capturedUris.size)
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
        } catch (e: Exception) { null }
    }

    fun reset() {
        capturedUris.clear()
        scannedBarcode = null
        sessionManualPrice = null
        sessionManualColor = null
        localAiStandardizedData = null
        deterministicApiMetadata = DeterministicApiMetadata()
        sessionRepository.setDiscoveryStatus(DiscoveryStatus())
        _uiState.value = BoxCaptureUiState.Idle()
    }
}
