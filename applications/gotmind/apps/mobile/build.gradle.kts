plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.android.application.firebase")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.gotmind.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.gotmind"
        versionCode = 7
        versionName = "0.0.7"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    implementation(project(":applications:gotmind:model"))
    implementation(project(":applications:gotmind:database"))
    implementation(project(":applications:gotmind:data"))
    implementation(project(":applications:gotmind:analytics"))
    implementation(project(":applications:gotmind:features:games"))
    implementation(project(":applications:gotmind:features:leaderboard"))
    implementation(project(":applications:gotmind:features:settings"))
    implementation(project(":applications:gotmind:features:memblox"))
    implementation(project(":applications:gotmind:features:mindwave"))

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigationsuite)
    implementation(libs.androidx.window.core)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
