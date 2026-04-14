plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.features.ai.configuration"
}

dependencies {
    // --- Shared Core Projects ---
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:data"))

    // --- Feature Dependencies ---
    implementation(project(":features:smartcapture"))

    // --- Third Party ---
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)
}
