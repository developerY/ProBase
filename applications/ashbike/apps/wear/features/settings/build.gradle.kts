plugins {
    // 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")

    // ✅ Required for Type-Safe Navigation & Nav3
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    // Unique namespace for the Wear Settings Feature
    namespace = "com.zoewave.ashbike.wear.settings"
}

dependencies {
    // --- Shared Projects ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))

    // --- Hardware Features ---
    implementation(project(":features:health"))
    implementation(project(":features:ble"))
    implementation(project(":features:nfc"))

    // ⚠️ WATCH OUT: Wear OS devices generally do not have cameras.
    // If your qrscanner module relies on CameraX or MLKit Vision,
    // it will likely crash or be unusable on a smartwatch.
    implementation(project(":features:qrscanner"))

    // --- AshBike Database & Models ---
    implementation(project(":applications:ashbike:database"))
    implementation(project(":applications:ashbike:model"))

    // ❌ REMOVED: implementation(project(":applications:ashbike:features:main"))
    // Reason: Never leak mobile-specific UI/features into the Wear OS module.

    // --- Serialization (The backbone of Nav3) ---
    implementation(libs.kotlinx.serialization.json)

    // --- Navigation 3 (Strict) ---
    // Note: navigation3.ui is removed. The NavDisplay is handled by :wear:app.
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // --- Maps & Location ---
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- Wear OS UI & Compose ---
    // ✅ Replaced implied mobile Material with Wear Material 3 & Foundation
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Lifecycle ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    // --- Collections ---
    implementation(libs.kotlinx.collections.immutable)

    // --- Hilt Dependency Injection ---
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.material)
    // Note: If your `composetemplate.android.hilt` plugin applies KSP automatically,
    // you can safely remove this line to avoid duplicate processing.
    ksp(libs.hilt.android.compiler)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}