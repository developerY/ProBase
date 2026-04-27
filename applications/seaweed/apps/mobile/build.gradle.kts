plugins {
    id("composetemplate.android.application")
    id("composetemplate.android.application.compose")
    id("composetemplate.android.hilt")
    id("composetemplate.android.application.firebase")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zoewave.probase.seaweed.mobile"

    defaultConfig {
        applicationId = "com.zoewave.probase.seaweed"
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    // ✅ 6. Enable Build Config (Disabled by default in AGP 9)
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":core:model"))

    implementation(project(":applications:seaweed:model"))
    implementation(project(":applications:seaweed:database"))
    implementation(project(":applications:seaweed:data"))
    implementation(project(":applications:seaweed:features:main"))
    implementation(project(":applications:seaweed:features:receiptcapture"))
    implementation(project(":applications:seaweed:features:spendingcontrol"))
    implementation(project(":features:payment:stripe"))
    implementation(project(":features:ai:capture"))
    implementation(project(":features:ai:configuration"))

    implementation(project(":applications:seaweed:apps:mobile:core"))
    implementation(project(":applications:seaweed:apps:mobile:features:home"))
    implementation(project(":applications:seaweed:apps:mobile:features:transaction"))
    implementation(project(":applications:seaweed:apps:mobile:features:settings"))
    implementation(project(":applications:seaweed:apps:mobile:features:bills"))
    implementation(project(":applications:seaweed:apps:mobile:features:budget"))
    implementation(project(":features:camera"))

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
    implementation(libs.hilt.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
