plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.goswift.wear"

    defaultConfig {
        applicationId = "com.zoewave.probase.goswift"
        versionCode = 1
        versionName = "0.0.1"
        minSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))

    implementation(project(":applications:goswift:model"))
    implementation(project(":applications:goswift:data"))
    implementation(project(":applications:goswift:features:main"))

    // Wear Feature Modules (to be created)
    implementation(project(":applications:goswift:apps:wear:features:home"))
    implementation(project(":applications:goswift:apps:wear:features:input"))

    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.google.play.services.wearable)

    // Wear UI & Navigation
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation3)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Horologist
    implementation(libs.horologist.compose.layout)
    implementation(libs.horologist.compose.tools)

    // Health Connect
    implementation(libs.androidx.health.connect.client)
}
