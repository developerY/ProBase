package com.zoewave.probase.kocolor.features.cosmetics.ui

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.data.CosmeticDefaults
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.features.analyzer.data.AnalyzerEngine
import com.zoewave.probase.kocolor.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

enum class SortOption {
    NEWEST, EXPIRY, COST_PER_USE, BRAND
}

data class CategoryMetadata(
    val itemCount: Int = 0,
    val totalValue: Double = 0.0,
    val representativeImageUrl: String? = null,
    val representativeColorHex: String? = null,
    val leadingBrand: String? = null,
    val averageFillLevel: Double? = null
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
    val categoryFilter: String? = null
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
}

@HiltViewModel
class CosmeticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cosmeticDao: CosmeticDao,
    private val sessionRepository: FashionSessionRepository,
    private val analyzerEngine: AnalyzerEngine,
    @Named("KoColor") private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    private val _aiResult = MutableStateFlow<CosmeticItem?>(null)
    private val _draftItem = MutableStateFlow(CosmeticItem(
        name = "", 
        brand = "", 
        macroCategory = MacroCategory.COMPLEXION, 
        microCategory = MicroCategory.FOUNDATION
    ))
    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    private val _categoryFilter = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            cosmeticDao.getAllCosmetics().first().let {
                if (it.isEmpty()) {
                    initializeDefaultCosmetics()
                }
            }
        }

        sessionRepository.lastScannedCode
            .filterNotNull()
            .onEach { code ->
                _draftItem.value = _draftItem.value.copy(batchCode = code)
                sessionRepository.setLastScannedCode(null) // Consume it
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initializeDefaultCosmetics() {
        for (item in CosmeticDefaults.getDefaultCosmetics()) {
            cosmeticDao.insertCosmetic(item)
        }
    }

    val uiState: StateFlow<CosmeticsUiState> = combine(
        cosmeticDao.getAllCosmetics(),
        sessionRepository.capturedItemUri,
        _isAnalyzing,
        _aiResult,
        _draftItem,
        _searchQuery,
        _sortOption,
        _categoryFilter
    ) { array ->
        val entities = array[0] as List<CosmeticItemEntity>
        val capturedUri = array[1] as String?
        val analyzing = array[2] as Boolean
        val aiResult = array[3] as CosmeticItem?
        val draft = array[4] as CosmeticItem
        val query = array[5] as String
        val sort = array[6] as SortOption
        val filter = array[7] as String?

        val models = entities.map { it.toModel() }
        val groupStats = entities.groupBy { it.macroCategory.displayName }.mapValues { it.value.size }
        
        val categoryMetadata = models.groupBy { it.macroCategory.displayName }.mapValues { (_, items) ->
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
                averageFillLevel = averageFill
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
            capturedImageUri = capturedUri,
            isAnalyzing = analyzing,
            aiResult = aiResult,
            draftItem = draft.copy(imageUrl = capturedUri ?: draft.imageUrl),
            searchQuery = query,
            sortOption = sort,
            totalCosmetics = models.size,
            expiringCosmeticsCount = expiringCount,
            cosmeticsByGroup = groupStats,
            categoriesMetadata = categoryMetadata,
            categoryFilter = filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CosmeticsUiState())

    fun onEvent(event: CosmeticsEvent) {
        when (event) {
            is CosmeticsEvent.AddItem -> {
                addItem(event.item)
                _draftItem.value = CosmeticItem(
                    name = "", 
                    brand = "", 
                    macroCategory = MacroCategory.COMPLEXION, 
                    microCategory = MicroCategory.FOUNDATION
                )
            }
            is CosmeticsEvent.UpdateItem -> updateItem(event.item)
            is CosmeticsEvent.DeleteItem -> deleteItem(event.id)
            is CosmeticsEvent.UseItem -> useItem(event.id)
            is CosmeticsEvent.UpdateDraft -> {
                val updatedItem = if (event.item.microCategory != _draftItem.value.microCategory) {
                    event.item.copy(amountPerUse = event.item.microCategory.typicalAmountPerUse)
                } else {
                    event.item
                }
                _draftItem.value = updatedItem
            }
            is CosmeticsEvent.StartEditing -> _draftItem.value = event.item
            is CosmeticsEvent.InitializeEdit -> {
                viewModelScope.launch {
                    cosmeticDao.getCosmeticById(event.itemId).first()?.let { entity ->
                        _draftItem.value = entity.toModel()
                    }
                }
            }
            is CosmeticsEvent.InitializeAdd -> {
                _categoryFilter.value = event.categoryFilter
                val macro = MacroCategory.entries.firstOrNull { 
                    it.displayName.contains(event.categoryFilter ?: "", ignoreCase = true) 
                } ?: MacroCategory.COMPLEXION
                
                val micro = MicroCategory.entries.firstOrNull { it.macro == macro } ?: MicroCategory.FOUNDATION
                
                _draftItem.value = CosmeticItem(
                    name = "", 
                    brand = "", 
                    macroCategory = macro, 
                    microCategory = micro,
                    amountPerUse = micro.typicalAmountPerUse
                )
            }
            is CosmeticsEvent.UpdateSearchQuery -> _searchQuery.value = event.query
            is CosmeticsEvent.UpdateSortOption -> _sortOption.value = event.option
            CosmeticsEvent.ScanWithGemini -> scanWithGemini()
            CosmeticsEvent.ClearCapturedImage -> sessionRepository.setCapturedItemUri(null)
            is CosmeticsEvent.HandleScanResult -> _draftItem.value = _draftItem.value.copy(batchCode = event.code)
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
            cosmeticDao.updateCosmetic(updated.toEntity())
        }
    }

    private fun updateItem(item: CosmeticItem) {
        viewModelScope.launch {
            cosmeticDao.updateCosmetic(item.toEntity())
        }
    }

    private fun scanWithGemini() {
        val uri = uiState.value.capturedImageUri ?: return
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
                    // Note: Auto-fill logic here might need to map Gemini categories to our new taxonomy
                    result?.let {
                        // Assuming AnalyzerEngine provides a model that needs to be mapped
                        // For now we keep existing simple copy if types match, or fix mapping
                        // In a real pro app, we'd have a mapping layer for Gemini outputs
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
            cosmeticDao.insertCosmetic(item.toEntity())
        }
    }

    private fun deleteItem(id: Long) {
        viewModelScope.launch {
            cosmeticDao.deleteCosmetic(id)
        }
    }
}
