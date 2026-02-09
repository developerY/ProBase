plugins {
    // 1. Conventions (Handles AGP 9.0, Java 21, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace for Zoewave architecture
    namespace = "com.zoewave.probase.features.ml"
}

dependencies {
    implementation(project(":core:data"))

    // androidx-lifecycle-viewmodel-compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.lifecycle.viewmodel.android)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt Dependency Injection
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    // implementation(libs.material.legacy)

    // coil
    implementation(libs.coil.compose)

    // ML
    implementation(libs.mlkit.text.recognition)

    // Add Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Preview
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}