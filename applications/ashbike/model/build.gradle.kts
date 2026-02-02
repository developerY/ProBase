plugins {
    // 1. Conventions
    id("composetemplate.android.library")

    // 2. Essential for Data Models
    alias(libs.plugins.jetbrains.kotlin.serialization) // For saving/loading JSON
    id("kotlin-parcelize") // For passing objects between Activities/Fragments
}

android {
    namespace = "com.zoewave.ashbike.model"
}

dependencies {
    implementation(project(":core:model"))
    // --- 1. Serialization ---
    // Allows BikeRide to be converted to JSON or passed in Navigation 3
    implementation(libs.kotlinx.serialization.json)

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