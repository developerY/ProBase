plugins {
    // 1. Conventions
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.kotlin.serialization")
    id("kotlin-parcelize")
}

// namespace = "com.zoewave.photodo.model"
android {
    namespace = "com.zoewave.photodo.model"
}

dependencies {
    implementation(project(":core:model"))
    // Maps
    implementation(libs.google.play.services.location)
    implementation(libs.google.maps.compose)

    // --- 2. Date & Time ---
    // Critical for "Ride Duration", "Start Time", "End Time"
    // implementation(libs.kotlinx.datetime)

    // Immutable Collections
    implementation(libs.kotlinx.collections.immutable)

    // Icons (Required for PhotoTodoRoute)
    implementation(libs.androidx.compose.material.icons.extended)
}

