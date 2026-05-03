plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.seaweed.wear.features.bills"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:database"))
    implementation(project(":applications:seaweed:data"))

    // Wear UI & Navigation
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Previews
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.wear.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
