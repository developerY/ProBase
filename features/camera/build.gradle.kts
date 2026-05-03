plugins {
    // 1. Conventions (Handles AGP 9.0, Java 21, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace for Zoewave architecture
    namespace = "com.zoewave.probase.features.camera"
}

dependencies {
    // --- 1. Shared Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:util")) // Use if still needed

    // --- 2. Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.lifecycle.viewmodel.android)

    // --- 3. UI & Compose ---
    // 'library.compose' plugin handles the BOM and basic tooling automatically.
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Legacy Material (Only keep if strictly necessary)
    // implementation(libs.material.legacy)

    // --- 4. CameraX ---
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    // CameraX Compose Viewfinder
    implementation(libs.androidx.camera.compose)
    // Permissions (Accompanist)
    implementation(libs.google.accompanist.permissions)

    // Coil for Safe Image Loading
    implementation(libs.coil.compose)

    // --- 4. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    // Compiler (ksp) is handled automatically by the Hilt Convention Plugin

    // --- 5. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}