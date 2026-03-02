plugins {
    // Swap this for your exact build-logic library plugin ID (e.g., "probase.android.library")
    alias(libs.plugins.android.library)
    id("composetemplate.android.hilt")
}

android {
    // Unique namespace for the mobile hardware/data layer
    namespace = "com.zoewave.ashbike.mobile.data.sensor"
}

dependencies {
    // --- Shared Projects ---
    // Contains the RideTrackingEngine interface
    implementation(project(":applications:ashbike:data"))

    // Contains the LocationPoint domain model
    implementation(project(":applications:ashbike:model"))

    // Contains your HeartRateRepository for the BLE chest strap
    implementation(project(":core:data"))

    // --- Mobile Hardware Sensors (GMS) ---
    // This replaces the Wear OS Health Services library.
    // It provides FusedLocationProviderClient for the phone.
    // --- Maps & Location (For the Dashboard/Speedometer) ---
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
