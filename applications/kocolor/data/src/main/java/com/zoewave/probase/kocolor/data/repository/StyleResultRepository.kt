package com.zoewave.probase.kocolor.data.repository

import com.zoewave.probase.kocolor.data.usecase.StyleCreationResult
import com.zoewave.probase.kocolor.data.usecase.StyleResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleResultRepository @Inject constructor() {

    private val _latestResult = MutableStateFlow<StyleResult?>(null)
    val latestResult: StateFlow<StyleResult?> = _latestResult.asStateFlow()

    private val _latestCreationResult = MutableStateFlow<StyleCreationResult?>(null)
    val latestCreationResult: StateFlow<StyleCreationResult?> = _latestCreationResult.asStateFlow()

    fun setLatestResult(result: StyleResult, creationResult: StyleCreationResult? = null) {
        _latestResult.value = result
        if (creationResult != null) {
            _latestCreationResult.value = creationResult
        }
    }
}
