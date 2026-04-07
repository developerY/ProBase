plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.library.compose")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.goswift.mobile.input"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":applications:goswift:model"))
    implementation(project(":applications:goswift:features:main"))
    implementation(project(":applications:goswift:apps:mobile:features:shots"))
    implementation(project(":applications:goswift:apps:mobile:features:hydration"))
    
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
