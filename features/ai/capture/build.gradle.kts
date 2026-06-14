plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.features.ai.capture"
}

dependencies {
    // --- Shared Core Projects ---
    api(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))

    // --- ML & AI ---
    implementation(libs.mlkit.text.recognition)
    api(libs.google.generative.ai)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)

    // --- Feature Dependencies ---
    implementation(project(":features:camera"))
    implementation(project(":features:compliance"))

    // --- Third Party ---
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation(libs.squareup.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.google.accompanist.permissions)
}
