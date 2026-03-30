plugins {
    id("composetemplate.android.library")
    // ✅ Required for Type-Safe Navigation
    id("composetemplate.kotlin.serialization")
}

android {
    namespace = "com.zoewave.probase.seaweed.model"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
}
