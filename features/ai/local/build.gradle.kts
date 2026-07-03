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
    
    implementation(libs.google.generative.ai)
    implementation(libs.google.ai.edge.aicore)
    implementation(libs.kotlinx.serialization.json)
}
