package com.zoewave.probase.kocolor.features.cosmetics.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.model.network.DiscoveryStatus
import com.zoewave.probase.core.model.network.ServiceStatus
import com.zoewave.probase.core.model.ritual.ArchiveStatus
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Formulation
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.core.model.ritual.InventorySource
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.features.analyzer.data.AnalyzerEngine
import com.zoewave.probase.kocolor.features.chemicals.domain.repository.ChemicalRepository
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.fda.data.repository.FdaRepository
import com.zoewave.probase.kocolor.features.makeupapi.domain.repository.MakeupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption {
    NEWEST, EXPIRY, COST_PER_USE, BRAND
}

data class CategoryMetadata(
    val itemCount: Int = 0,
    val totalValue: Double = 0.0,
    val representativeImageUrl: String? = null,
    val representativeColorHex: String? = null,
    val leadingBrand: String? = null,
    val averageFillLevel: Double? = null,
    val description: String? = null
)

data class CosmeticsUiState(
    val items: List<CosmeticItem> = emptyList(),
    val filteredItems: List<CosmeticItem> = emptyList(),
    val isLoading: Boolean = true,
    val capturedImageUri: String? = null,
    val aiResult: CosmeticItem? = null,
    val draftItem: CosmeticItem = CosmeticItem(
        name = "", 
        brand = "", 
        macroCategory = MacroCategory.COMPLEXION, 
        microCategory = MicroCategory.FOUNDATION,
        colorHex = "#FFFFFF"
    ),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NEWEST,
    val totalCosmetics: Int = 0,
    val expiringCosmeticsCount: Int = 0,
    val cosmeticsByGroup: Map<String, Int> = emptyMap(),
    val categoriesMetadata: Map<String, CategoryMetadata> = emptyMap(),
    val categoryFilter: String? = null,
    val scanStatus: String? = null,
    val scanState: FashionSessionRepository.ScanStatus = FashionSessionRepository.ScanStatus.IDLE,
    val discoveryStatus: DiscoveryStatus = DiscoveryStatus(),
    val isObfContributionEnabled: Boolean = false,
    val uvIndex: Double = 0.0,
    val archiveStatuses: Map<Long, ArchiveStatus> = emptyMap()
) {
    val canContributeToObf: Boolean get() = !draftItem.batchCode.isNullOrBlank()
    val isScanSuccessful: Boolean get() = scanState == FashionSessionRepository.ScanStatus.SUCCESS
    val isScanIncomplete: Boolean get() = scanState == FashionSessionRepository.ScanStatus.INCOMPLETE
    val lastScanFailed: Boolean get() = scanState == FashionSessionRepository.ScanStatus.FAILED
    val isAnalyzing: Boolean get() = scanState == FashionSessionRepository.ScanStatus.ANALYZING
}

sealed class CosmeticsEvent {
    data class AddItem(val item: CosmeticItem) : CosmeticsEvent()
    data class UpdateItem(val item: CosmeticItem) : CosmeticsEvent()
    data class DeleteItem(val id: Long) : CosmeticsEvent()
    data class UseItem(val id: Long) : CosmeticsEvent()
    data class UpdateDraft(val item: CosmeticItem) : CosmeticsEvent()
    data class UpdateSearchQuery(val query: String) : CosmeticsEvent()
    data class UpdateSortOption(val option: SortOption) : CosmeticsEvent()
    data object ScanWithGemini : CosmeticsEvent()
    data object ClearCapturedImage : CosmeticsEvent()
    data class StartEditing(val item: CosmeticItem) : CosmeticsEvent()
    data class InitializeEdit(val itemId: Long) : CosmeticsEvent()
    data class InitializeAdd(val categoryFilter: String?) : CosmeticsEvent()
    data class HandleScanResult(val code: String) : CosmeticsEvent()
    data class OnObfContributionToggled(val enabled: Boolean) : CosmeticsEvent()
    data object AcknowledgeErrorDialog : CosmeticsEvent()
    data object ResetScanState : CosmeticsEvent()
    data object ContinueToReview : CosmeticsEvent()
    data object CancelDiscovery : CosmeticsEvent()
    data class CloneToPersonal(val item: CosmeticItem) : CosmeticsEvent()
}

@HiltViewModel
class CosmeticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val sessionRepository: FashionSessionRepository,
    private val fdaRepository: FdaRepository,
    private val chemicalRepository: ChemicalRepository,
    private val makeupRepository: MakeupRepository,
    private val weatherRepo: WeatherRepo,
    private val analyzerEngine: AnalyzerEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _aiResult = MutableStateFlow<CosmeticItem?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    private val _categoryFilter = MutableStateFlow<String?>(null)
    private val _scanStatus = MutableStateFlow<String?>(null)
    private val _isObfContributionEnabled = MutableStateFlow(false)
    private val _uvIndex = MutableStateFlow(0.0)
    private val _archiveStatuses = MutableStateFlow<Map<Long, ArchiveStatus>>(emptyMap())

    init {
        fetchWeather()

        // Initialize session draft if empty
        if (sessionRepository.cosmeticDraft.value == null) {
            sessionRepository.setCosmeticDraft(CosmeticItem(
                name = "", 
                brand = "", 
                macroCategory = MacroCategory.COMPLEXION, 
                microCategory = MicroCategory.FOUNDATION,
                colorHex = "#FFFFFF"
            ))
        }

        // Auto-lookup on barcode scan
        sessionRepository.lastScannedCode
            .filterNotNull()
            .onEach { code ->
                updateSessionDraft { it.copy(batchCode = code) }
                fetchObfProduct(code)
                sessionRepository.setLastScannedCode(null)
            }
            .launchIn(viewModelScope)
            
        // Auto-update on image capture
        sessionRepository.capturedItemUri
            .filterNotNull()
            .onEach { uri ->
                updateSessionDraft { it.copy(imageUrl = uri) }
                scanWithGemini()
            }
            .launchIn(viewModelScope)
    }

    val uiState: StateFlow<CosmeticsUiState> = combine(
        cosmeticRepository.getAllCosmetics(),
        _aiResult,
        sessionRepository.cosmeticDraft.filterNotNull(),
        sessionRepository.scanState,
        sessionRepository.discoveryStatus,
        _searchQuery,
        _sortOption,
        _categoryFilter,
        _scanStatus,
        _isObfContributionEnabled,
        _uvIndex,
        _archiveStatuses
    ) { array ->
        val models = array[0] as List<CosmeticItem>
        val aiResult = array[1] as CosmeticItem?
        val draft = array[2] as CosmeticItem
        val scanState = array[3] as FashionSessionRepository.ScanStatus
        val discStatus = array[4] as DiscoveryStatus
        val query = array[5] as String
        val sort = array[6] as SortOption
        val filter = array[7] as String?
        val scanStatus = array[8] as String?
        val contributionEnabled = array[9] as Boolean
        val uvVal = array[10] as Double
        val archiveStatuses = array[11] as Map<Long, ArchiveStatus>

        val groupStats = models.groupBy { it.macroCategory.displayName }.mapValues { it.value.size }
        
        val categoryMetadata = models.groupBy { it.macroCategory.displayName }.mapValues { (name, items) ->
            val macro = items.firstOrNull()?.macroCategory
            val representativeItem = items.filter { it.imageUrl != null }.maxByOrNull { it.usageCount } ?: items.maxByOrNull { it.usageCount }
            val brands = items.map { it.brand }.groupBy { it }.mapValues { it.value.size }
            val leadingBrand = brands.maxByOrNull { it.value }?.key
            val fillLevels = items.mapNotNull { it.fillLevel }
            val averageFill = if (fillLevels.isEmpty()) null else fillLevels.average()

            CategoryMetadata(
                itemCount = items.size,
                totalValue = items.sumOf { it.price ?: 0.0 },
                representativeImageUrl = representativeItem?.imageUrl,
                representativeColorHex = representativeItem?.colorHex,
                leadingBrand = leadingBrand,
                averageFillLevel = averageFill,
                description = macro?.description
            )
        }
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val now = System.currentTimeMillis()
        val expiringCount = models.count { item ->
            item.estimatedExpiry?.let { expiry ->
                (expiry - now) in 0..thirtyDaysInMillis
            } ?: false
        }

        val filtered = models.filter {
            it.name.contains(query, ignoreCase = true) || 
            it.brand.contains(query, ignoreCase = true) ||
            it.microCategory.displayName.contains(query, ignoreCase = true) ||
            it.macroCategory.displayName.contains(query, ignoreCase = true)
        }.let { list ->
            when (sort) {
                SortOption.NEWEST -> list.sortedByDescending { it.timestamp }
                SortOption.EXPIRY -> list.sortedBy { it.estimatedExpiry ?: Long.MAX_VALUE }
                SortOption.COST_PER_USE -> list.sortedByDescending { it.costPerUse ?: 0.0 }
                SortOption.BRAND -> list.sortedBy { it.brand }
            }
        }

        CosmeticsUiState(
            items = models,
            filteredItems = filtered,
            isLoading = false,
            capturedImageUri = draft.imageUrl,
            aiResult = aiResult,
            draftItem = draft,
            searchQuery = query,
            sortOption = sort,
            totalCosmetics = models.size,
            expiringCosmeticsCount = expiringCount,
            cosmeticsByGroup = groupStats,
            categoriesMetadata = categoryMetadata,
            categoryFilter = filter,
            scanStatus = scanStatus,
            scanState = scanState,
            discoveryStatus = discStatus,
            isObfContributionEnabled = contributionEnabled,
            uvIndex = uvVal,
            archiveStatuses = archiveStatuses
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CosmeticsUiState())

    fun onEvent(event: CosmeticsEvent) {
        when (event) {
            is CosmeticsEvent.AddItem -> {
                val userItem = event.item.copy(
                    internalId = 0L,
                    sourceType = InventorySource.USER_SCAN,
                    sourceName = "My Archive",
                    sourcePackId = null
                )
                addItem(userItem)
                
                if (_isObfContributionEnabled.value && !userItem.batchCode.isNullOrBlank()) {
                    contributeToObf(userItem)
                }

                sessionRepository.reset()
                _scanStatus.value = null
            }
            is CosmeticsEvent.UpdateItem -> {
                updateItem(event.item)
                sessionRepository.reset()
                _scanStatus.value = null
            }
            is CosmeticsEvent.DeleteItem -> deleteItem(event.id)
            is CosmeticsEvent.UseItem -> useItem(event.id)
            is CosmeticsEvent.UpdateDraft -> {
                val current = sessionRepository.cosmeticDraft.value
                val updatedItem = if (event.item.microCategory != (current?.microCategory ?: MicroCategory.FOUNDATION)) {
                    event.item.copy(amountPerUse = event.item.microCategory.typicalAmountPerUse)
                } else {
                    event.item
                }
                sessionRepository.setCosmeticDraft(updatedItem)
            }
            is CosmeticsEvent.StartEditing -> sessionRepository.setCosmeticDraft(event.item)
            is CosmeticsEvent.InitializeEdit -> {
                viewModelScope.launch {
                    cosmeticRepository.getAllCosmetics().map { items -> 
                        items.find { it.internalId == event.itemId } 
                    }.filterNotNull().first().let { model ->
                        sessionRepository.setCosmeticDraft(model)
                    }
                }
            }
            is CosmeticsEvent.InitializeAdd -> {
                _categoryFilter.value = event.categoryFilter
                
                val currentDraft = sessionRepository.cosmeticDraft.value
                val isEmpty = currentDraft == null || (currentDraft.name.isBlank() && 
                             currentDraft.brand.isBlank() && 
                             currentDraft.imageUrl == null && 
                             currentDraft.batchCode == null &&
                             currentDraft.internalId == 0L)
                
                if (!isEmpty || sessionRepository.scanState.value == FashionSessionRepository.ScanStatus.ANALYZING || sessionRepository.scanState.value == FashionSessionRepository.ScanStatus.SUCCESS) return
                
                val macro = MacroCategory.entries.firstOrNull { 
                    it.displayName.contains(event.categoryFilter ?: "", ignoreCase = true) 
                } ?: MacroCategory.COMPLEXION
                
                val micro = MicroCategory.entries.firstOrNull { it.macro == macro } ?: MicroCategory.FOUNDATION
                
                sessionRepository.setCosmeticDraft(CosmeticItem(
                    name = "", 
                    brand = "", 
                    macroCategory = macro, 
                    microCategory = micro,
                    colorHex = "#FFFFFF",
                    amountPerUse = micro.typicalAmountPerUse
                ))
            }
            is CosmeticsEvent.UpdateSearchQuery -> _searchQuery.value = event.query
            is CosmeticsEvent.UpdateSortOption -> _sortOption.value = event.option
            CosmeticsEvent.ScanWithGemini -> scanWithGemini()
            CosmeticsEvent.ClearCapturedImage -> sessionRepository.setCapturedItemUri(null)
            is CosmeticsEvent.HandleScanResult -> fetchObfProduct(event.code)
            is CosmeticsEvent.OnObfContributionToggled -> {
                _isObfContributionEnabled.value = event.enabled
            }
            CosmeticsEvent.AcknowledgeErrorDialog -> {
                _scanStatus.value = null
            }
            CosmeticsEvent.ResetScanState -> {
                sessionRepository.setScanState(FashionSessionRepository.ScanStatus.IDLE)
                _scanStatus.value = null
            }
            CosmeticsEvent.CancelDiscovery -> {
                sessionRepository.reset()
                _scanStatus.value = null
            }
            CosmeticsEvent.ContinueToReview -> {
                sessionRepository.setScanState(FashionSessionRepository.ScanStatus.SUCCESS)
            }
            is CosmeticsEvent.CloneToPersonal -> {
                cloneToPersonal(event.item)
            }
        }
    }

    private fun contributeToObf(item: CosmeticItem) {
        viewModelScope.launch {
            Log.d("CosmeticsVM", "Contributing ${item.name} to OBF...")
            // Actual implementation would call repository.contribute(item)
        }
    }

    private fun updateSessionDraft(block: (CosmeticItem) -> CosmeticItem) {
        val current = sessionRepository.cosmeticDraft.value ?: return
        sessionRepository.setCosmeticDraft(block(current))
    }

    private fun fetchObfProduct(code: String) {
        viewModelScope.launch {
            sessionRepository.setScanState(FashionSessionRepository.ScanStatus.ANALYZING)
            sessionRepository.updateServiceStatus("obf", ServiceStatus.ACCESSING, "Querying barcode: $code")
            _scanStatus.value = context.getString(R.string.applications_kocolor_features_cosmetics_searching_obf)
            
            updateSessionDraft { it.copy(batchCode = code) }
            
            val result = cosmeticRepository.fetchProductByBarcode(code)
            
            result.onSuccess { obfItem ->
                sessionRepository.updateServiceStatus("obf", ServiceStatus.SUCCESS, "Found: ${obfItem.name}")
                _scanStatus.value = context.getString(R.string.applications_kocolor_features_cosmetics_product_found_format, obfItem.name)
                
                updateSessionDraft { current ->
                    current.copy(
                        batchCode = code,
                        name = obfItem.name.takeIf { it.isNotBlank() && it != context.getString(R.string.applications_kocolor_features_cosmetics_unknown_product) } ?: current.name,
                        brand = obfItem.brand.takeIf { it.isNotBlank() && it != context.getString(R.string.applications_kocolor_features_cosmetics_unknown_brand) } ?: current.brand,
                        macroCategory = if (obfItem.macroCategory != MacroCategory.TOOLS) obfItem.macroCategory else current.macroCategory,
                        microCategory = if (obfItem.microCategory != MicroCategory.AI_PENDING) obfItem.microCategory else current.microCategory,
                        notes = obfItem.notes ?: current.notes,
                        volume = obfItem.volume ?: current.volume,
                        imageUrl = current.imageUrl ?: obfItem.imageUrl,
                        
                        // Carry over derived facets & ingredients
                        formulation = obfItem.formulation,
                        finish = obfItem.finish,
                        chemistryBase = obfItem.chemistryBase,
                        heroIngredient = obfItem.heroIngredient,
                        ingredients = obfItem.ingredients,
                        allergens = obfItem.allergens,
                        containsFragrance = obfItem.containsFragrance,
                        ecoScore = obfItem.ecoScore,
                        isVegan = obfItem.isVegan,
                        isCrueltyFree = obfItem.isCrueltyFree
                    )
                }
                
                // ASYNC CLINICAL SAFETY FETCH (FDA)
                viewModelScope.launch {
                    sessionRepository.updateServiceStatus("fda", ServiceStatus.ACCESSING, "Checking clinical recalls...")
                    val draft = sessionRepository.cosmeticDraft.value ?: return@launch
                    val recall = fdaRepository.getRecalls(draft.brand, draft.name)
                    val eventCount = fdaRepository.getAdverseEventsCount(draft.brand, draft.name)
                    val topReactions = fdaRepository.getTopReactions(draft.brand, draft.name)
                    val label = fdaRepository.getDrugLabel(code)

                    if (recall != null || eventCount > 0 || label != null) {
                        sessionRepository.updateServiceStatus("fda", ServiceStatus.SUCCESS, "Safety data verified.")
                    } else {
                        sessionRepository.updateServiceStatus("fda", ServiceStatus.FAILED, "No clinical data found.")
                    }

                    // ASYNC CHEMICAL INTELLIGENCE (PubChem)
                    sessionRepository.updateServiceStatus("chemdb", ServiceStatus.ACCESSING, "Analyzing ${obfItem.ingredients.firstOrNull() ?: "ingredients"}...")
                    val topIngredient = obfItem.ingredients.firstOrNull()
                    val chemicalInfo = topIngredient?.let { chemicalRepository.getChemicalInfo(it).getOrNull() }

                    if (chemicalInfo != null) {
                        sessionRepository.updateServiceStatus("chemdb", ServiceStatus.SUCCESS, "Hazards: ${chemicalInfo.safetyHazards.size} detected.")
                    } else {
                        sessionRepository.updateServiceStatus("chemdb", ServiceStatus.FAILED, "No hazard data found.")
                    }

                    // ASYNC CATALOG CONTEXT (Makeup API)
                    sessionRepository.updateServiceStatus("makeupapi", ServiceStatus.ACCESSING, "Matching brand: ${draft.brand}")
                    val catalogResult = makeupRepository.searchProducts(brand = draft.brand)
                    catalogResult.onSuccess {
                        sessionRepository.updateServiceStatus("makeupapi", ServiceStatus.SUCCESS, "Found ${it.size} catalog matches.")
                    }.onFailure {
                        sessionRepository.updateServiceStatus("makeupapi", ServiceStatus.FAILED, "Brand not in catalog.")
                    }

                    updateSessionDraft { current ->
                        current.copy(
                            fdaRecallStatus = recall?.status,
                            fdaAdverseEventCount = eventCount,
                            fdaTopReactions = topReactions,
                            fdaClinicalWarnings = label?.warnings ?: emptyList(),
                            fdaActiveIngredients = label?.active_ingredient ?: emptyList(),
                            fdaDataVerified = true,
                            // Enrich with chemDB info if found
                            heroIngredient = chemicalInfo?.name ?: current.heroIngredient,
                            notes = (current.notes ?: "") + (chemicalInfo?.safetyHazards?.joinToString("\n")?.let { "\nSafety: $it" } ?: "")
                        )
                    }
                }
                
                // --- Confidence Threshold Logic ---
                val hasIngredients = obfItem.ingredients.isNotEmpty()
                val isComplete = hasIngredients && obfItem.name.isNotBlank() && obfItem.brand.isNotBlank()

                if (isComplete) {
                    // We stay in ANALYZING (Discovery screen) until CONTINUE is pressed
                    Log.d("CosmeticsVM", "Data complete. Staying on Discovery screen.")
                } else {
                    _scanStatus.value = "Incomplete data. Scan box to enrich."
                }
            }.onFailure {
                sessionRepository.updateServiceStatus("obf", ServiceStatus.FAILED, "Product not found in OBF.")
                _scanStatus.value = context.getString(R.string.applications_kocolor_features_cosmetics_product_not_found_obf)
                sessionRepository.setScanState(FashionSessionRepository.ScanStatus.FAILED)
            }
        }
    }

    private fun useItem(id: Long) {
        viewModelScope.launch {
            val item = uiState.value.items.find { it.internalId == id } ?: return@launch
            val updated = item.copy(
                usageCount = item.usageCount + 1,
                isOpened = true,
                openedDate = item.openedDate ?: System.currentTimeMillis()
            )
            cosmeticRepository.saveCosmeticItem(updated)
        }
    }

    private fun updateItem(item: CosmeticItem) {
        viewModelScope.launch {
            cosmeticRepository.saveCosmeticItem(item)
        }
    }

    private fun scanWithGemini() {
        val uri = sessionRepository.cosmeticDraft.value?.imageUrl ?: return
        viewModelScope.launch {
            sessionRepository.setScanState(FashionSessionRepository.ScanStatus.ANALYZING)
            sessionRepository.updateServiceStatus("gemini", ServiceStatus.ACCESSING, "Uploading image to Gemini...")
            _aiResult.value = null
            
            val apiKey = aiSettings.getGeminiApiKey()

            if (!apiKey.isNullOrBlank()) {
                val bitmap = loadBitmapFromUri(uri.toUri())
                if (bitmap != null) {
                    val result = analyzerEngine.analyzeCosmeticProduct(
                        image = bitmap,
                        apiKey = apiKey
                    )
                    _aiResult.value = result
                    
                    if (result != null) {
                        sessionRepository.updateServiceStatus("gemini", ServiceStatus.SUCCESS, "Analysis complete.")
                        updateSessionDraft { current ->
                            current.copy(
                                name = if (current.name.isBlank()) result.name else current.name,
                                brand = if (current.brand.isBlank()) result.brand else current.brand,
                                macroCategory = if (result.macroCategory != MacroCategory.TOOLS) result.macroCategory else current.macroCategory,
                                microCategory = if (result.microCategory != MicroCategory.AI_PENDING) result.microCategory else current.microCategory,
                                colorHex = current.colorHex ?: result.colorHex,
                                shadeName = current.shadeName ?: result.shadeName,
                                
                                // Carry over AI discovered facets
                                heroIngredient = result.heroIngredient ?: current.heroIngredient,
                                ingredients = result.ingredients.takeIf { it.isNotEmpty() } ?: current.ingredients,
                                containsFragrance = result.containsFragrance ?: current.containsFragrance,
                                formulation = if (result.formulation != Formulation.UNKNOWN) result.formulation else current.formulation,
                                finish = if (result.finish != Finish.UNKNOWN) result.finish else current.finish
                            )
                        }
                        sessionRepository.setScanState(FashionSessionRepository.ScanStatus.SUCCESS)
                    } else {
                        sessionRepository.updateServiceStatus("gemini", ServiceStatus.FAILED, "AI returned null.")
                        sessionRepository.setScanState(FashionSessionRepository.ScanStatus.FAILED)
                    }
                }
            } else {
                sessionRepository.updateServiceStatus("gemini", ServiceStatus.FAILED, "API Key missing.")
                sessionRepository.setScanState(FashionSessionRepository.ScanStatus.IDLE)
            }
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

    private fun addItem(item: CosmeticItem) {
        viewModelScope.launch {
            cosmeticRepository.saveCosmeticItem(item)
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            cosmeticRepository.deleteCosmeticItem(id)
        }
    }

    private fun cloneToPersonal(item: CosmeticItem) {
        if (_archiveStatuses.value[item.internalId] == ArchiveStatus.SUCCESS || 
            _archiveStatuses.value[item.internalId] == ArchiveStatus.ARCHIVING) return

        viewModelScope.launch {
            _archiveStatuses.update { it + (item.internalId to ArchiveStatus.ARCHIVING) }
            
            try {
                // Use the DAO's optimized SQL cloning logic
                cosmeticRepository.cloneToPersonalArchive(item.internalId)
                
                // Keep the success state visible for a moment for UX
                _archiveStatuses.update { it + (item.internalId to ArchiveStatus.SUCCESS) }
            } catch (e: Exception) {
                Log.e("CosmeticsVM", "Cloning failed", e)
                _archiveStatuses.update { it + (item.internalId to ArchiveStatus.ERROR) }
            }
        }
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            try {
                val city = "Santa Barbara, US"
                val weather = weatherRepo.openCurrentWeatherByCity(city)
                val lat = weather?.coord?.lat
                val lon = weather?.coord?.lon
                if (lat != null && lon != null) {
                    val env = weatherRepo.getEnvironmentalContext(lat, lon)
                    _uvIndex.value = env?.uvIndex ?: 0.0
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
