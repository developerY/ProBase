plugins {
    // 1. Conventions
    id("composetemplate.android.library")
    id("composetemplate.kotlin.serialization")

    // 2. Essential for Data Models
    id("kotlin-parcelize") // For passing objects between Activities/Fragments
}

android {
    namespace = "com.zoewave.ashbike.model"
}

dependencies {
    implementation(project(":core:model"))
    // Maps
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- 2. Date & Time ---
    // Critical for "Ride Duration", "Start Time", "End Time"
    // implementation(libs.kotlinx.datetime)

    // --- 3. Immutable Collections ---
    // Recommended for Compose stability (e.g. List<LocationPoint>)
    implementation(libs.kotlinx.collections.immutable)
}