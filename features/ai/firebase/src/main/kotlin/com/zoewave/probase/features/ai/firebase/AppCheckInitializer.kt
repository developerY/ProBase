package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppCheckInitializer {
    fun initialize(context: Context) {
        // 1. Lifecycle Safety: Always use application context so Firebase does not retain an Activity context
        val appContext = context.applicationContext
        // val testing = true

        // 2. Identify if the current device is an Android Emulator
        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86")

        // 3. DEBUG -> Debug App Check Provider, RELEASE -> Play Integrity App Check Provider
        val providerFactory = if (BuildConfig.DEBUG && isEmulator) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            // Note: For physical debug builds to work, ensure your local debug.keystore
            // SHA-256 is added to your Firebase Project Settings.
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        try {
            Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
        } catch (e: IllegalStateException) {
            // Prevents crashes if initialize() is accidentally called twice
        }
    }
}
