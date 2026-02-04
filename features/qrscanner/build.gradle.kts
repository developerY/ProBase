plugins {
    // 1. Conventions (Handles AGP 9.0, Java 21, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace for Zoewave architecture
    namespace = "com.zoewave.probase.features.qrscanner"
}

dependencies {
    // --- 1. Shared Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))

    // Check if you still need these legacy core modules:
    implementation(project(":core:database"))
    // implementation(project(":core:util"))

    // --- 2. Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    //implementation(libs.androidx.lifecycle.viewmodel.android)

    // --- 3. UI & Compose ---
    // 'library.compose' plugin handles the BOM and basic tooling.
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Legacy Material (Only keep if strictly necessary)
    // implementation(libs.material.legacy)

    // --- 4. Feature Specific ---
    implementation(libs.gms.play.services.code.scanner)

    // --- 5. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Compiler (ksp) is handled automatically by the Hilt Convention Plugin

    // --- 6. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}