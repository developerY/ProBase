plugins {
    // 1. Conventions (Handles AGP 9.0, Java 21, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace for Zoewave
    namespace = "com.zoewave.probase.features.places"
}

dependencies {
    // --- 1. Shared Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:network"))

    // If you have specific UI components in core:ui, include this too:
    // implementation(project(":core:ui"))

    // --- 2. Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- 3. UI & Compose ---
    // Note: 'library.compose' plugin adds the BOM and basic tooling automatically.
    implementation(libs.androidx.compose.material3)

    // Legacy Material (Only keep if you strictly need old XML views)
    // implementation(libs.material.legacy)

    // --- 4. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Compiler is handled automatically by the Hilt Convention Plugin

    // --- 5. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}