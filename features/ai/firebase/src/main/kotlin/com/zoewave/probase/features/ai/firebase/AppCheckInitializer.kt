package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppCheckInitializer {
    fun initialize(context: Context) {
        // 1. Lifecycle Safety: Always use application context so Firebase does not retain an Activity context
        val appContext = context.applicationContext

        // 2. Identify if the current device is an Android Emulator
        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86")

        // 3. DEBUG on Emulators -> Debug App Check Provider
        //    ALL PHYSICAL DEVICES -> Play Integrity App Check Provider
        val providerFactory = if (BuildConfig.DEBUG && isEmulator) {
            Log.d("AppCheckInitializer", "Emulator detected. Initializing DEBUG App Check Provider")
            DebugAppCheckProviderFactory.getInstance()
        } else {
            Log.d("AppCheckInitializer", "Physical device (or Release) detected. Initializing Play Integrity App Check Provider")
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
