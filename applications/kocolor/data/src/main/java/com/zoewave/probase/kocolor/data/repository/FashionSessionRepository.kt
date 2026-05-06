package com.zoewave.probase.kocolor.data.repository

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FashionSessionRepository @Inject constructor() {
    private val _faceUri = MutableStateFlow<String?>(null)
    val faceUri: StateFlow<String?> = _faceUri.asStateFlow()

    private val _clothesUri = MutableStateFlow<String?>(null)
    val clothesUri: StateFlow<String?> = _clothesUri.asStateFlow()

    fun setFaceUri(uri: String?) {
        Log.d("KoColorSession", "Setting Face URI: $uri")
        _faceUri.value = uri
    }

    fun setClothesUri(uri: String?) {
        Log.d("KoColorSession", "Setting Clothes URI: $uri")
        _clothesUri.value = uri
    }

    fun reset() {
        Log.d("KoColorSession", "Resetting session")
        _faceUri.value = null
        _clothesUri.value = null
    }
}
