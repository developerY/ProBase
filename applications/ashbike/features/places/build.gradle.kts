plugins {
    // 1. Conventions (Handles AGP 9.0, Java 21, Compose, Hilt)
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    // 2. Updated Namespace
    namespace = "com.zoewave.ashbike.features.places"
}

dependencies {
    // --- 1. Shared Modules ---
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))

    // AshBike Specific Data (BikeRepository, etc.)
    implementation(project(":applications:ashbike:data"))

    // --- 2. Feature Dependencies ---
    // Mapping old ":feature:..." to new ":apps:mobile:features:..." structure
    // implementation(project(":apps:mobile:features:health")) // Fixed typo 'heatlh'
    // implementation(project(":apps:mobile:features:nfc"))
    // implementation(project(":apps:mobile:features:weather"))

    // --- 3. Core Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    // --- 4. UI & Compose ---
    // Note: 'library.compose' plugin handles BOM & Tooling
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    // Legacy Material (Only keep if strictly necessary)
    // Profiling/Tracing
    implementation("androidx.compose.runtime:runtime-tracing")

    // --- 5. Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    // Compiler is handled by the Hilt Convention Plugin

    // --- 6. Maps & Location ---
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- 7. Specialized Libs ---
    // Permissions (Accompanist)
    implementation(libs.google.accompanist.permissions)
    // Health Connect
    implementation(libs.androidx.health.connect.client)
    // Collections
    implementation(libs.kotlinx.collections.immutable)

    // --- 8. Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}