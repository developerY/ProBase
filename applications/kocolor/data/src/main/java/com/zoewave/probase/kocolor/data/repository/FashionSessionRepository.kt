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

    private val _hairUri = MutableStateFlow<String?>(null)
    val hairUri: StateFlow<String?> = _hairUri.asStateFlow()

    private val _shoesUri = MutableStateFlow<String?>(null)
    val shoesUri: StateFlow<String?> = _shoesUri.asStateFlow()

    private val _clothesUri = MutableStateFlow<String?>(null)
    val clothesUri: StateFlow<String?> = _clothesUri.asStateFlow()

    private val _capturedItemUri = MutableStateFlow<String?>(null)
    val capturedItemUri: StateFlow<String?> = _capturedItemUri.asStateFlow()

    private val _location = MutableStateFlow<String?>(null)
    val location: StateFlow<String?> = _location.asStateFlow()

    fun setFaceUri(uri: String?) {
        Log.d("KoColorSession", "Setting Face URI: $uri")
        _faceUri.value = uri
    }

    fun setHairUri(uri: String?) {
        Log.d("KoColorSession", "Setting Hair URI: $uri")
        _hairUri.value = uri
    }

    fun setShoesUri(uri: String?) {
        Log.d("KoColorSession", "Setting Shoes URI: $uri")
        _shoesUri.value = uri
    }

    fun setClothesUri(uri: String?) {
        Log.d("KoColorSession", "Setting Clothes URI: $uri")
        _clothesUri.value = uri
    }

    fun setCapturedItemUri(uri: String?) {
        Log.d("KoColorSession", "Setting Captured Item URI: $uri")
        _capturedItemUri.value = uri
    }

    fun reset() {
        Log.d("KoColorSession", "Resetting session")
        _faceUri.value = null
        _hairUri.value = null
        _shoesUri.value = null
        _clothesUri.value = null
    }
}
