package com.zoewave.probase.features.ai.local.data

import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import com.google.android.play.core.assetpacks.model.AssetPackStorageMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelAssetManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val assetPackManager: AssetPackManager = AssetPackManagerFactory.getInstance(context)
    private val packName = "ai_model_pack"
    private val modelFileName = "model.litertlm"

    private val _localModelState = MutableStateFlow<LocalModelState>(LocalModelState.NotDownloaded)
    val localModelState: StateFlow<LocalModelState> = _localModelState.asStateFlow()

    private val listener = AssetPackStateUpdateListener { state ->
        when (state.status()) {
            AssetPackStatus.PENDING, AssetPackStatus.DOWNLOADING -> {
                val downloaded = state.bytesDownloaded()
                val total = state.totalBytesToDownload()
                val pct = if (total > 0) ((downloaded.toFloat() / total) * 100).toInt() else 0
                _localModelState.value = LocalModelState.Downloading(downloaded, total, pct)
            }
            AssetPackStatus.COMPLETED -> {
                val path = getModelPath()
                if (path != null) {
                    _localModelState.value = LocalModelState.Ready(path, "1.0")
                } else {
                    _localModelState.value = LocalModelState.Error("Pack completed but file not found", true)
                }
            }
            AssetPackStatus.FAILED -> {
                _localModelState.value = LocalModelState.Error("Error Code: ${state.errorCode()}", true)
            }
            AssetPackStatus.CANCELED -> {
                _localModelState.value = LocalModelState.NotDownloaded
            }
        }
    }

    init {
        checkModelExistence()
        assetPackManager.registerListener(listener)
    }

    fun checkModelExistence() {
        val path = getModelPath()
        if (path != null && File(path).exists()) {
            _localModelState.value = LocalModelState.Ready(path, "1.0")
        } else {
            _localModelState.value = LocalModelState.NotDownloaded
        }
    }

    fun getModelPath(): String? {
        val packLocation = assetPackManager.getPackLocation(packName)
        return if (packLocation != null && packLocation.packStorageMethod() == AssetPackStorageMethod.STORAGE_FILES) {
            val assetsPath = packLocation.assetsPath()
            val modelFile = File(assetsPath, modelFileName)
            if (modelFile.exists()) modelFile.absolutePath else null
        } else null
    }

    fun startDownload() {
        assetPackManager.fetch(listOf(packName))
    }

    fun cancelDownload() {
        assetPackManager.cancel(listOf(packName))
    }

    fun deleteModel() {
        assetPackManager.removePack(packName)
        _localModelState.value = LocalModelState.NotDownloaded
    }
}
