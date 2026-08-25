package com.zoewave.probase.kocolor.mobile

import android.app.Application
import com.zoewave.probase.features.ai.firebase.AppCheckInitializer
import com.zoewave.probase.features.ai.firebase.FirebaseAiAuthManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class KoColorApp : Application() {

    @Inject lateinit var authManager: FirebaseAiAuthManager

    override fun onCreate() {
        super.onCreate()
        AppCheckInitializer.initialize(this)
        
        // Ensure user is authenticated anonymously for Firebase AI Logic
        MainScope().launch {
            authManager.signInAnonymously()
        }
    }
}
