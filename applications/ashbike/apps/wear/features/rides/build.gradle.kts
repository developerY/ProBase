plugins {
    // 1. Apply Convention Plugins (Library, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")

    // ✅ Required for Type-Safe Navigation & Nav3
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    // Unique namespace for the Wear Rides Feature
    namespace = "com.zoewave.ashbike.wear.rides"
}

dependencies {
    // --- Shared Projects ---
    // These should be form-factor agnostic (pure Kotlin or shared logic)
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":features:health"))

    // --- AshBike Data / Database ---
    implementation(project(":applications:ashbike:database"))
    implementation(project(":applications:ashbike:model"))
    implementation(project(":applications:ashbike:features:places"))
    implementation(project(":applications:ashbike:features:main"))


    // --- Serialization (The backbone of Nav3) ---
    implementation(libs.kotlinx.serialization.json)

    // --- Navigation 3 (Strict) ---
    // Note: We omit navigation3.ui here because the App module handles the display.
    // This feature module only needs runtime to define the NavKeys.
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // --- Maps & Location (For the Dashboard/Speedometer) ---
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- Health & Wear Sensors ---
    // ✅ Replaced Health Connect with Health Services for live hardware sensor reading
    implementation(libs.androidx.health.services.client)

    // --- Wear OS UI & Compose ---
    // ✅ Use Wear Material 3 and Wear Foundation instead of Mobile Material
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Note: The Hilt Convention Plugin handles 'ksp(libs.hilt.compiler)' automatically

    // --- Lifecycle ---
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    // --- Collections ---
    implementation(libs.kotlinx.collections.immutable)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}