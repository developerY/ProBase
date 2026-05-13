plugins {
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.rxlogic.features.medications"
}

dependencies {
    implementation(project(":applications:rxlogic:model"))
    implementation(project(":applications:rxlogic:data"))
    implementation(project(":core:ui"))
    
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.android.compiler)
}
