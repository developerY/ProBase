plugins {
    // 1. Conventions (Handles AGP 9.0, Java 21, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace for Zoewave
    namespace = "com.zoewave.probase.ashbike.feature.weather"
}

dependencies {
    // --- 1. Shared Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))

    // Check if you still need these legacy modules in the new arch:
    implementation(project(":core:database"))
    implementation(project(":core:util"))

    // --- 2. Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.android)

    // --- 3. UI & Compose ---
    // Note: 'library.compose' plugin adds the BOM and basic tooling.
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Legacy Material (Only keep if you strictly need old XML views or Theme.MaterialComponents)
    implementation(libs.material.legacy)

    // --- 4. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    // Compiler is handled automatically by the Hilt Convention Plugin

    // --- 5. Maps & Location ---
    implementation(libs.google.play.services.maps)
    // implementation(libs.play.services.location) // Uncomment if fetching raw location here

    // --- 6. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}