plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.features.smartcapture"
}

dependencies {
    // --- Shared Core Projects ---
    api(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))

    // --- ML & AI ---
    implementation(libs.mlkit.text.recognition)
    implementation(libs.google.generative.ai)

    // --- Core Features ---
    implementation(project(":features:camera"))

    // --- Third Party ---
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.google.accompanist.permissions)
}
