plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}

android {
    namespace = "com.zoewave.probase.gotmind.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":applications:gotmind:model"))
    implementation(project(":applications:gotmind:database"))
    implementation(project(":core:data"))
    implementation(project(":core:util"))
    implementation(libs.androidx.core.ktx)
}
