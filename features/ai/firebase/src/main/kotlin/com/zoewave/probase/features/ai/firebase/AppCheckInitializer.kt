package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppCheckInitializer {
    fun initialize(context: Context) {
        // 1. Lifecycle Safety: Always use application context so Firebase does not retain an Activity context
        val appContext = context.applicationContext

        // 2. Base App Check provider selection strictly on whether the app is a debug build:
        //    When debuggable -> ALWAYS install DebugAppCheckProviderFactory (physical devices & emulators)
        //    When !debuggable -> Install PlayIntegrityAppCheckProviderFactory (release builds)
        val isDebuggable = BuildConfig.DEBUG || (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val providerFactory = if (isDebuggable) {
            Log.i("AppCheckInitializer", "Debug build detected. Installing DebugAppCheckProviderFactory.")
            DebugAppCheckProviderFactory.getInstance()
        } else {
            Log.i("AppCheckInitializer", "Release build detected. Installing PlayIntegrityAppCheckProviderFactory.")
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
