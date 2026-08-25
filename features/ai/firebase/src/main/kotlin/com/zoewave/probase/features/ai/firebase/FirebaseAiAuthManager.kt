package com.zoewave.probase.features.ai.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiAuthManager @Inject constructor() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    suspend fun signInAnonymously() {
        if (_isAuthenticated.value || Firebase.auth.currentUser != null) {
            _isAuthenticated.value = true
            Log.d("FirebaseAiAuth", "User already authenticated: ${Firebase.auth.currentUser?.uid}")
            return
        }

        try {
            Log.d("FirebaseAiAuth", "Attempting anonymous sign-in...")
            val result = Firebase.auth.signInAnonymously().await()
            _isAuthenticated.value = true
            Log.d("FirebaseAiAuth", "Anonymous sign-in success: ${result.user?.uid}")
        } catch (e: Exception) {
            Log.e("FirebaseAiAuth", "Anonymous sign-in failed", e)
            _isAuthenticated.value = false
        }
    }
}
