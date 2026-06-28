package com.zoewave.probase.kocolor.features.cosmetics.ui

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Formulation
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.core.network.repository.weather.WeatherRepo
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.features.analyzer.data.AnalyzerEngine
import com.zoewave.probase.kocolor.features.cosmetics.R
import com.zoewave.probase.kocolor.features.fda.data.repository.FdaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
    val isAnalyzing: Boolean = false,
    val aiResult: CosmeticItem? = null,
    val draftItem: CosmeticItem = CosmeticItem(
        name = "", 
        brand = "", 
        macroCategory = MacroCategory.COMPLEXION, 
        microCategory = MicroCategory.FOUNDATION
    ),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.NEWEST,
    val totalCosmetics: Int = 0,
    val expiringCosmeticsCount: Int = 0,
    val cosmeticsByGroup: Map<String, Int> = emptyMap(),
    val categoriesMetadata: Map<String, CategoryMetadata> = emptyMap(),
    val categoryFilter: String? = null,
    val scanStatus: String? = null,
    val isScanSuccessful: Boolean = false,
    val lastScanFailed: Boolean = false,
    val uvIndex: Double = 0.0
)

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
    data object ResetScanState : CosmeticsEvent()
    data object CancelDiscovery : CosmeticsEvent()
}

@HiltViewModel
class CosmeticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val sessionRepository: FashionSessionRepository,
    private val fdaRepository: FdaRepository,
    private val weatherRepo: WeatherRepo,
    private val analyzerEngine: AnalyzerEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    private val _aiResult = MutableStateFlow<CosmeticItem?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    private val _categoryFilter = MutableStateFlow<String?>(null)
    private val _scanStatus = MutableStateFlow<String?>(null)
    private val _isScanSuccessful = MutableStateFlow(false)
    private val _lastScanFailed = MutableStateFlow(false)
    private val _uvIndex = MutableStateFlow(0.0)

    init {
        fetchWeather()

        // Initialize session draft if empty
        if (sessionRepository.cosmeticDraft.value == null) {
            sessionRepository.setCosmeticDraft(CosmeticItem(
                name = "", 
                brand = "", 
                macroCategory = MacroCategory.COMPLEXION, 
                microCategory = MicroCategory.FOUNDATION
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
        _isAnalyzing,
        _aiResult,
        sessionRepository.cosmeticDraft.filterNotNull(),
        _searchQuery,
        _sortOption,
        _categoryFilter,
        _scanStatus,
        _isScanSuccessful,
        _lastScanFailed,
        _uvIndex
    ) { array ->
        val models = array[0] as List<CosmeticItem>
        val analyzing = array[1] as Boolean
        val aiResult = array[2] as CosmeticItem?
        val draft = array[3] as CosmeticItem
        val query = array[4] as String
        val sort = array[5] as SortOption
        val filter = array[6] as String?
        val scanStatus = array[7] as String?
        val scanSuccessful = array[8] as Boolean
        val scanFailed = array[9] as Boolean
        val uvVal = array[10] as Double

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
            isAnalyzing = analyzing,
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
            isScanSuccessful = scanSuccessful,
            lastScanFailed = scanFailed,
            uvIndex = uvVal
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CosmeticsUiState())

    fun onEvent(event: CosmeticsEvent) {
        when (event) {
            is CosmeticsEvent.AddItem -> {
                addItem(event.item)
                sessionRepository.reset()
                _isScanSuccessful.value = false
                _lastScanFailed.value = false
                _scanStatus.value = null
            }
            is CosmeticsEvent.UpdateItem -> updateItem(event.item)
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
                        items.find { it.id == event.itemId } 
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
                             currentDraft.batchCode == null)
                
                if (!isEmpty || _isAnalyzing.value || _isScanSuccessful.value) return
                
                val macro = MacroCategory.entries.firstOrNull { 
                    it.displayName.contains(event.categoryFilter ?: "", ignoreCase = true) 
                } ?: MacroCategory.COMPLEXION
                
                val micro = MicroCategory.entries.firstOrNull { it.macro == macro } ?: MicroCategory.FOUNDATION
                
                sessionRepository.setCosmeticDraft(CosmeticItem(
                    name = "", 
                    brand = "", 
                    macroCategory = macro, 
                    microCategory = micro,
                    amountPerUse = micro.typicalAmountPerUse
                ))
            }
            is CosmeticsEvent.UpdateSearchQuery -> _searchQuery.value = event.query
            is CosmeticsEvent.UpdateSortOption -> _sortOption.value = event.option
            CosmeticsEvent.ScanWithGemini -> scanWithGemini()
            CosmeticsEvent.ClearCapturedImage -> sessionRepository.setCapturedItemUri(null)
            is CosmeticsEvent.HandleScanResult -> fetchObfProduct(event.code)
            CosmeticsEvent.ResetScanState -> {
                _isScanSuccessful.value = false
                _lastScanFailed.value = false
                _scanStatus.value = null
            }
            CosmeticsEvent.CancelDiscovery -> {
                sessionRepository.reset()
                _isScanSuccessful.value = false
                _lastScanFailed.value = false
                _scanStatus.value = null
            }
        }
    }

    private fun updateSessionDraft(block: (CosmeticItem) -> CosmeticItem) {
        val current = sessionRepository.cosmeticDraft.value ?: return
        sessionRepository.setCosmeticDraft(block(current))
    }

    private fun fetchObfProduct(code: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _scanStatus.value = context.getString(R.string.applications_kocolor_features_cosmetics_searching_obf)
            _lastScanFailed.value = false
            
            updateSessionDraft { it.copy(batchCode = code) }
            
            val result = cosmeticRepository.fetchProductByBarcode(code)
            
            result.onSuccess { obfItem ->
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
                    val draft = sessionRepository.cosmeticDraft.value ?: return@launch
                    val recall = fdaRepository.getRecalls(draft.brand, draft.name)
                    val eventCount = fdaRepository.getAdverseEventsCount(draft.brand, draft.name)
                    val topReactions = fdaRepository.getTopReactions(draft.brand, draft.name)
                    val label = fdaRepository.getDrugLabel(code)

                    updateSessionDraft { current ->
                        current.copy(
                            fdaRecallStatus = recall?.status,
                            fdaAdverseEventCount = eventCount,
                            fdaTopReactions = topReactions,
                            fdaClinicalWarnings = label?.warnings ?: emptyList(),
                            fdaActiveIngredients = label?.active_ingredient ?: emptyList(),
                            isFdaChecked = true
                        )
                    }
                }
                
                _isScanSuccessful.value = true
            }.onFailure {
                _scanStatus.value = context.getString(R.string.applications_kocolor_features_cosmetics_product_not_found_obf)
                _lastScanFailed.value = true
            }
            _isAnalyzing.value = false
            
            delay(3000)
            _scanStatus.value = null
        }
    }

    private fun useItem(id: Long) {
        viewModelScope.launch {
            val item = uiState.value.items.find { it.id == id } ?: return@launch
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
            _isAnalyzing.value = true
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
                    
                    result?.let { aiItem ->
                        updateSessionDraft { current ->
                            current.copy(
                                name = if (current.name.isBlank()) aiItem.name else current.name,
                                brand = if (current.brand.isBlank()) aiItem.brand else current.brand,
                                macroCategory = if (aiItem.macroCategory != MacroCategory.TOOLS) aiItem.macroCategory else current.macroCategory,
                                microCategory = if (aiItem.microCategory != MicroCategory.AI_PENDING) aiItem.microCategory else current.microCategory,
                                colorHex = current.colorHex ?: aiItem.colorHex,
                                shadeName = current.shadeName ?: aiItem.shadeName,
                                
                                // Carry over AI discovered facets
                                heroIngredient = aiItem.heroIngredient ?: current.heroIngredient,
                                ingredients = aiItem.ingredients.takeIf { it.isNotEmpty() } ?: current.ingredients,
                                containsFragrance = aiItem.containsFragrance ?: current.containsFragrance,
                                formulation = if (aiItem.formulation != Formulation.UNKNOWN) aiItem.formulation else current.formulation,
                                finish = if (aiItem.finish != Finish.UNKNOWN) aiItem.finish else current.finish
                            )
                        }
                    }
                }
            }
            _isAnalyzing.value = false
        }
    }

    private fun loadBitmapFromUri(uri: Uri): android.graphics.Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            android.graphics.BitmapFactory.decodeStream(inputStream)
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
