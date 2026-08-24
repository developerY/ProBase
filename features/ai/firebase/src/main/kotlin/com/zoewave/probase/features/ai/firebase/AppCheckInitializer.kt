package com.zoewave.probase.features.ai.firebase

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

object AppCheckInitializer {
    fun initialize(context: Context) {
        Firebase.initialize(context)
        
        val providerFactory = if (BuildConfig.DEBUG) {
            // Generates a debug token for local testing without Play Integrity requirements
            DebugAppCheckProviderFactory.getInstance()
        } else {
            // Enforces Play Integrity device attestation for production releases
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
    }
}
