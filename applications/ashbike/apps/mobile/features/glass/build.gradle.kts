plugins {
    // 1. Conventions (Handles AGP, Kotlin, Java Toolchains, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace for Zoewave
    namespace = "com.zoewave.ashbike.mobile.glass"

    defaultConfig {
        // If this module runs on Glass/XR devices, ensure minSdk supports it.
        // The convention plugin usually sets a safe default, but you can override here if needed.
        // minSdk = 36
    }
}

dependencies {
    // --- 1. Shared Logic ---
    implementation(project(":core:model"))
    // Use the AshBike-specific data module (contains BikeRepository)
    implementation(project(":applications:ashbike:data"))
    implementation(project(":applications:ashbike:model"))

    // --- 2. Android XR / Glass (Glimmer) ---
    // Keeping these as they appear to be specific XR libraries in your catalog
    implementation(libs.androidx.glimmer)
    implementation(libs.androidx.projected)

    // --- 3. Firebase (AI / Vertex) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)

    // --- 4. Standard UI & Compose ---
    // Note: 'library.compose' plugin adds the BOM and basic UI/Tooling automatically.
    // We only add specific extras here.
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3) // If using M3 components alongside Glimmer
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- 5. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Note: The Hilt Convention Plugin handles 'ksp(libs.hilt.compiler)' automatically

    // --- 6. Lifecycle ---
    implementation(libs.androidx.lifecycle.process)

    // --- 7. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}