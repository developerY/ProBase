plugins {
    id("composetemplate.android.library")
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.goswift.model"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
}
