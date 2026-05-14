plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.kotlin.serialization")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.kocolor.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.kocolor"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))
    
    implementation(project(":applications:kocolor:model"))
    implementation(project(":applications:kocolor:db"))
    implementation(project(":applications:kocolor:data"))
    implementation(project(":applications:kocolor:data"))
    
    implementation(project(":applications:kocolor:apps:mobile:core"))
    implementation(project(":applications:kocolor:apps:mobile:features:home"))
    implementation(project(":applications:kocolor:apps:mobile:features:settings"))
    
    implementation(project(":applications:kocolor:features:analyzer"))
    implementation(project(":applications:kocolor:features:suggestions"))
    implementation(project(":applications:kocolor:features:color"))
    implementation(project(":applications:kocolor:features:inventory"))
    implementation(project(":applications:kocolor:features:routines"))
    implementation(project(":features:ar:naillab"))
    implementation(project(":features:ar:facelab"))
    
    implementation(project(":features:ai:configuration"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:camera"))

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigationsuite)
    implementation(libs.androidx.compose.material3.window.size)

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
