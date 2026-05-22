plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.seaweed.mobile.smartcamera"
}

dependencies {
    implementation(project(":core:util"))
    implementation(project(":core:ui"))
    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:data"))
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:vision"))
    
    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.compose)

    implementation(libs.google.accompanist.permissions)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // AI SDK (Gemini)
    implementation(libs.google.generative.ai)
}
