plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.features.readers.ocr"
}

dependencies {
    implementation(project(":core:util"))
    
    // ML Kit Text Recognition
    implementation(libs.mlkit.text.recognition)
    implementation(libs.kotlinx.coroutines.play.services)
}
