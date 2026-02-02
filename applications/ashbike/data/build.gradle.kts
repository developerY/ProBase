plugins {
    // 1. Apply Conventions (Library & Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")

    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.ashbike.data"
}

dependencies {
    // --- 1. Local AshBike Modules ---
    // Access to the Ride History DB
    implementation(project(":applications:ashbike:database"))
    implementation(project(":applications:ashbike:model"))

    // --- 2. Global Core Platform ---
    // Access to Shared Models (BikeRideInfo) and Generic Managers (Sensors, Bluetooth)
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))

    // --- 3. Architecture Components ---
    // Coroutines (For Repository & Service async work)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Lifecycle (Required for BikeForegroundService)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- 4. Hilt (Dependency Injection) ---
    implementation(libs.hilt.android)
    // Note: The 'composetemplate.android.hilt' plugin usually applies the KSP plugin,
    // but we explicitly add the compiler here to be safe.
    // Hilt
    ksp(libs.hilt.android.compiler)

    // --- 5. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}