plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.android.application.firebase")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.goswift.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.goswift"
        versionCode = 1
        versionName = "0.0.1"
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
    implementation(project(":applications:goswift:database"))
    implementation(project(":applications:goswift:data"))
    implementation(project(":applications:goswift:features:main"))
    implementation(project(":applications:goswift:apps:mobile:features:home"))
    implementation(project(":applications:goswift:apps:mobile:features:shots"))
    implementation(project(":applications:goswift:apps:mobile:features:settings"))
    implementation(project(":applications:goswift:apps:mobile:features:hydration"))
    implementation(project(":applications:goswift:apps:mobile:features:input"))

    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
