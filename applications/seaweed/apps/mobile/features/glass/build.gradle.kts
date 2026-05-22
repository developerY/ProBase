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

    implementation(libs.androidx.glimmer.core)
    implementation(libs.androidx.glimmer.google.fonts)
    implementation(libs.androidx.projected)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
}
