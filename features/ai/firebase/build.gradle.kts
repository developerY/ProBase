plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.features.ai.firebase"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:util"))

    // --- Firebase AI Logic ---
    api(platform(libs.firebase.bom))
    api(libs.firebase.ai)

    // --- Hilt ---
    implementation(libs.hilt.android)
}
