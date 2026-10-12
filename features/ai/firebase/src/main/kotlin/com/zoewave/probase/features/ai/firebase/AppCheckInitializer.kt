package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppCheckInitializer {
    fun initialize(context: Context) {
        // 1. Lifecycle Safety: Always use application context so Firebase does not retain an Activity context
        val appContext = context.applicationContext

        // 2. DEBUG -> Debug App Check Provider for local testing on emulators and physical debug hardware
        //    RELEASE -> Play Integrity App Check Provider for Play Store production distribution
        val providerFactory = if (BuildConfig.DEBUG) {
            Log.d("AppCheckInitializer", "Initializing DEBUG App Check Provider")
            DebugAppCheckProviderFactory.getInstance()
        } else {
            Log.d("AppCheckInitializer", "Initializing RELEASE Play Integrity App Check Provider")
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        try {
            Log.d("AppCheckInitializer", "Installing AppCheckProviderFactory...")
            Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
            Log.d("AppCheckInitializer", "AppCheckProviderFactory installed successfully.")
        } catch (e: IllegalStateException) {
            Log.w("AppCheckInitializer", "AppCheckProviderFactory already installed. Skipping.")
        } catch (e: Exception) {
            Log.e("AppCheckInitializer", "Failed to install AppCheckProviderFactory", e)
        }
    }
}
