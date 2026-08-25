plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.kocolor.features.analyzer"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:local"))
    implementation(project(":features:ai:firebase"))
    implementation(project(":features:camera"))
    implementation(project(":features:readers:ocr"))
    implementation(project(":features:graphics"))

    implementation(libs.google.generative.ai)
    implementation(libs.google.play.services.maps)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation(libs.kotlinx.serialization.json)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ML Kit Face Detection
    implementation(libs.mlkit.face.detection)

    // ML Kit - Moved to :features:readers:ocr
    // implementation(libs.mlkit.text.recognition)
    // implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
