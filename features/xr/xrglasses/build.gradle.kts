plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.features.xr.xrglasses"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    
    implementation(libs.androidx.xr.runtime)
    implementation(libs.androidx.xr.arcore)
    implementation(libs.androidx.xr.compose)
    implementation(libs.androidx.xr.scenecore)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
