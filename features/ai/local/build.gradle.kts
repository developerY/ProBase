plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
    id("composetemplate.android.library.compose")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.features.ai.local"
}

dependencies {
    implementation(project(":core:util"))
    implementation(project(":features:readers:ocr"))

    // ML Kit for the initial OCR "Scoop"
    implementation(libs.mlkit.gms.text.recognition)
    implementation(libs.kotlinx.coroutines.play.services)
    
    // Raw AI Edge SDK (Bypassing the ML Kit Prompt wrapper)
    implementation(libs.google.ai.edge.aicore)

    // ML Kit GenAI Prompt (Required for existing LocalAiEngine.kt)
    implementation(libs.mlkit.genai.prompt)
    
    // Coroutines & Additional Compose basics (UI, Material3, BOM are handled by plugin)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
}
