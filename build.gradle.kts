plugins {
    // 1. Android Plugins (AGP 9.1.0-alpha07)
    // Locking AGP versions is still safe and recommended at the root
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false

    // 3. Compose Compiler
    // The new specific Compose Compiler plugin (Kotlin 2.0+)
    alias(libs.plugins.kotlin.compose) apply false

    // 4. Performance & Baseline Profiles
    alias(libs.plugins.androidx.baselineprofile) apply false

    // 5. Google Services (Optional, but good for locking)
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
    alias(libs.plugins.mapsplatform.secrets) apply false

    // ⚠️ REMOVED: alias(libs.plugins.jetbrains.kotlin.android)
    // AGP 9 will handle the Kotlin interaction in the modules directly.
    // Do NOT apply serialization here either if it causes similar conflicts.
    // ❌ REMOVED: Kotlin Android (Causes AGP 9 conflicts)
    // ❌ REMOVED: KSP (Transitively pulls in Kotlin -> Conflicts)
    // ❌ REMOVED: Hilt (Transitively pulls in KSP/Kotlin -> Conflicts)
}