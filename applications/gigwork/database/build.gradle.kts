plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
    id("composetemplate.android.room")
}

android {
    namespace = "com.zoewave.probase.gigwork.database"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":applications:gigwork:model"))
    implementation(libs.androidx.core.ktx)
}
