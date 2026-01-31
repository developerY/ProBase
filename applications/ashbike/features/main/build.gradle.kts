plugins {
    // 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    // ✅ Required for Type-Safe Navigation
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.zoewave.probase.ashbike.features.main"
}

dependencies {
    // --- Shared Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    // implementation(project(":core:util"))

    // --- Serialization (The backbone of Nav3) ---
    implementation(libs.kotlinx.serialization.json)

    // --- Navigation 3 (Strict) ---
    // We use the exact accessors generated from your TOML keys
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // --- UI Components ---
    // ✅ FIXED: Correct accessor for icons based on your TOML
    // Key: androidx-compose-material-icons-extended -> libs.androidx.compose.material.icons.extended
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Hilt ---
    // Note: Standard Hilt works fine. We don't need 'hilt-navigation-compose' (Nav2)
    // unless you are scoping ViewModels to backstack entries, which Nav3 handles differently.
    // implementation(libs.hilt.navigation.compose)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}