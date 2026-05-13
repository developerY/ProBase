plugins {
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.rxlogic.features.settings"
}

dependencies {
    implementation(project(":applications:rxlogic:model"))
    implementation(project(":applications:rxlogic:data"))
    implementation(project(":core:ui"))
    
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
}
