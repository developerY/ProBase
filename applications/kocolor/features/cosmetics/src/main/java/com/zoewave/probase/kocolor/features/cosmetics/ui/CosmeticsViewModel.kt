package com.zoewave.probase.kocolor.features.cosmetics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.data.CosmeticDefaults
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.features.analyzer.data.AnalyzerEngine
import com.zoewave.probase.kocolor.model.CosmeticItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CosmeticsUiState(
    val items: List<CosmeticItem> = emptyList(),
    val isLoading: Boolean = true,
    val capturedImageUri: String? = null,
    val isAnalyzing: Boolean = false,
    val aiResult: CosmeticItem? = null
)

sealed class CosmeticsEvent {
    data class AddItem(val item: CosmeticItem) : CosmeticsEvent()
    data class DeleteItem(val id: Long) : CosmeticsEvent()
    data object ScanWithGemini : CosmeticsEvent()
    data object ClearCapturedImage : CosmeticsEvent()
}

@HiltViewModel
class CosmeticsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cosmeticDao: CosmeticDao,
    private val sessionRepository: FashionSessionRepository,
    private val analyzerEngine: AnalyzerEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _isAnalyzing = MutableStateFlow(false)
    private val _aiResult = MutableStateFlow<CosmeticItem?>(null)

    init {
        viewModelScope.launch {
            cosmeticDao.getAllCosmetics().first().let {
                if (it.isEmpty()) {
                    initializeDefaultCosmetics()
                }
            }
        }
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
        _aiResult
    ) { entities, capturedUri, analyzing, aiResult ->
        CosmeticsUiState(
            items = entities.map { it.toModel() },
            isLoading = false,
            capturedImageUri = capturedUri,
            isAnalyzing = analyzing,
            aiResult = aiResult
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CosmeticsUiState())

    fun onEvent(event: CosmeticsEvent) {
        when (event) {
            is CosmeticsEvent.AddItem -> addItem(event.item)
            is CosmeticsEvent.DeleteItem -> deleteItem(event.id)
            CosmeticsEvent.ScanWithGemini -> scanWithGemini()
            CosmeticsEvent.ClearCapturedImage -> sessionRepository.setCapturedItemUri(null)
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

    private fun CosmeticItemEntity.toModel() = CosmeticItem(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        shadeName = shadeName,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp
    )

    private fun CosmeticItem.toEntity() = CosmeticItemEntity(
        id = id,
        name = name,
        brand = brand,
        category = category,
        colorHex = colorHex,
        shadeName = shadeName,
        imageUrl = imageUrl,
        notes = notes,
        timestamp = timestamp
    )
}
