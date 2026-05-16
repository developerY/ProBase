plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
}

android {
    namespace = "com.zoewave.probase.features.graphics"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
}
