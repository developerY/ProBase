plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
}

android {
    namespace = "com.zoewave.probase.features.microphone"
}

dependencies {
    implementation(project(":core:ui"))
    
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
