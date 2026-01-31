plugins {
    // 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")

    // 2. Apply Serialization Plugin
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.zoewave.probase.ashbike.features.main"
}

dependencies {
    // --- Project Modules ---
    // implementation(project(":core:model"))
    implementation(project(":core:ui"))   // For shared themes/components
    // implementation(project(":core:util")) // For standard utilities

    // --- Serialization (Required for Nav3) ---
    implementation(libs.kotlinx.serialization.json)

    // --- UI & Navigation ---
    // Note: 'composetemplate' adds basic Compose, but we add specific needs here
    implementation(libs.androidx.compose.material.icons.extended) // For the specific icons in your design


    // ✅ ADD THESE (The Nav3 libraries from your TOML)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Note: If you still need Hilt integration, check if 'libs.hilt.navigation.compose'
    // is compatible or if you need to update that as well.

    // --- Hilt ---
    // (Core Hilt is added by the plugin, but if you use Hilt Navigation)
    implementation(libs.hilt.navigation.compose)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}