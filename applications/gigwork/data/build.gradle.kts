plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.gigwork.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":applications:gigwork:model"))
    implementation(project(":applications:gigwork:database"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))
    implementation(libs.androidx.core.ktx)
}
