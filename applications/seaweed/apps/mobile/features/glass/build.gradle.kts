plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.seaweed.mobile.glass"
}

dependencies {
    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:data"))
    
    // AI & Gemini
    implementation(project(":features:ai:firebase"))
    implementation(project(":features:ai:vision"))
    implementation(libs.google.generative.ai)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    
    // ML Kit
    implementation(libs.mlkit.text.recognition)

    implementation(libs.androidx.glimmer.core)
    implementation(libs.androidx.glimmer.google.fonts)
    implementation(libs.androidx.projected)

    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.compose)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
}
