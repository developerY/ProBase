plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.goswift.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":applications:goswift:model"))
    implementation(project(":applications:goswift:database"))
    implementation(project(":core:data"))
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.core.ktx)
}
