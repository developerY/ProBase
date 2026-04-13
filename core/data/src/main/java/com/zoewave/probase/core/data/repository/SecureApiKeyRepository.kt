package com.zoewave.probase.core.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SecureApiKeyRepository @Inject constructor(
    @Named("SecureStorage") private val securePrefs: SharedPreferences
) {
    private companion object {
        const val GEMINI_API_KEY = "GEMINI_API_KEY"
    }

    private val _isKeySet = MutableStateFlow(!securePrefs.getString(GEMINI_API_KEY, null).isNullOrBlank())
    val isKeySetFlow = _isKeySet.asStateFlow()

    fun saveKey(key: String) {
        securePrefs.edit().putString(GEMINI_API_KEY, key).apply()
        _isKeySet.value = true
    }

    fun getKey(): String? {
        return securePrefs.getString(GEMINI_API_KEY, null)
    }

    fun deleteKey() {
        securePrefs.edit().remove(GEMINI_API_KEY).apply()
        _isKeySet.value = false
    }
}
