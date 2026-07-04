plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.features.ai.local"
}

dependencies {
    implementation(project(":core:util"))
    
    // THE TRUE ZERO-FOOTPRINT DEPENDENCY: Interfacing with Android System AICore
    implementation(libs.google.ai.edge.aicore)
    implementation(libs.kotlinx.serialization.json)
}
